package com.fiap.demo.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.dto.AuthResponse;
import com.fiap.demo.auth.dto.LoginRequest;
import com.fiap.demo.auth.dto.RegisterRequest;
import com.fiap.demo.auth.dto.RecoveryRequest;
import com.fiap.demo.auth.dto.ResetPasswordRequest;
import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.model.Role;
import com.fiap.demo.auth.model.PasswordRecovery;
import com.fiap.demo.auth.model.AcessoLog;
import com.fiap.demo.auth.repository.UsuarioRepository;
import com.fiap.demo.auth.repository.PasswordRecoveryRepository;
import com.fiap.demo.auth.repository.AcessoLogRepository;
import com.fiap.demo.auth.util.Sha256Util;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS = 5;

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordRecoveryRepository recoveryRepository;
    private final AcessoLogRepository acessoLogRepository;
    private final SmsService smsService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public AuthService(
            UsuarioRepository usuarioRepository,
            TokenService tokenService,
            PasswordRecoveryRepository recoveryRepository,
            AcessoLogRepository acessoLogRepository,
            SmsService smsService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.recoveryRepository = recoveryRepository;
        this.acessoLogRepository = acessoLogRepository;
        this.smsService = smsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cpf = CpfUtils.normalize(request.cpf());
        if (!CpfUtils.isValid(cpf)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CPF invalido");
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF ja cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .cpf(cpf)
                .senhaHash(Sha256Util.hash(request.senha()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .areaCultivoHectares(request.areaCultivo())
                .role(Role.ROLE_USER)
                .build();
        usuarioRepository.save(usuario);
        
        // Simular envio de SMS para cadastro
        smsService.enviarSms(cpf, "Bem-vindo ao Agro-GS! Seu cadastro foi concluido com sucesso.");
        
        return new AuthResponse(tokenService.generateToken(cpf), TokenService.TOKEN_EXP_HOURS, true);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse login(LoginRequest request) {
        String cpf = CpfUtils.normalize(request.cpf());
        String ip = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : "127.0.0.1";
        LocalDateTime agora = LocalDateTime.now();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpf);
        if (usuarioOpt.isEmpty()) {
            registrarAcesso(cpf, "FAILURE", ip, agora);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.estaBloqueado(agora)) {
            registrarAcesso(cpf, "FAILURE", ip, agora);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Conta bloqueada");
        }

        if (!Sha256Util.matches(request.senha(), usuario.getSenhaHash())) {
            usuario.registrarFalhaLogin();
            registrarAcesso(cpf, "FAILURE", ip, agora);
            
            int restantes = MAX_TENTATIVAS - usuario.getTentativasFalhasLogin();
            if (usuario.getTentativasFalhasLogin() >= MAX_TENTATIVAS) {
                usuario.setBloqueadoAte(agora.plusMinutes(30));
                usuarioRepository.save(usuario);
                smsService.enviarSms(cpf, "Sua conta foi bloqueada devido a multiplas tentativas de login incorretas.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta. Tentativas restantes: 0");
            }
            usuarioRepository.save(usuario);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta. Tentativas restantes: " + restantes);
        }

        usuario.resetarTentativasFalhas();
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        registrarAcesso(cpf, "SUCCESS", ip, agora);

        return new AuthResponse(tokenService.generateToken(cpf), TokenService.TOKEN_EXP_HOURS, false);
    }

    private void registrarAcesso(String cpf, String status, String ip, LocalDateTime timestamp) {
        AcessoLog log = AcessoLog.builder()
                .cpf(cpf)
                .status(status)
                .ipCliente(ip)
                .timestamp(timestamp)
                .build();
        acessoLogRepository.save(log);
    }

    @Transactional
    public void requestRecovery(RecoveryRequest request) {
        String cpf = CpfUtils.normalize(request.cpf());
        if (!CpfUtils.isValid(cpf)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CPF invalido");
        }
        if (!usuarioRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
        }

        LocalDateTime agora = LocalDateTime.now();
        PasswordRecovery recovery = recoveryRepository.findByCpf(cpf)
                .orElse(PasswordRecovery.builder()
                        .cpf(cpf)
                        .tentativasFalhas(0)
                        .build());

        if (recovery.estaBloqueado(agora)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Fluxo de recuperacao bloqueado. Tente mais tarde.");
        }

        // Gerar OTP de 6 dígitos
        String otp = String.format("%06d", new Random().nextInt(1000000));
        recovery.setOtp(otp);
        recovery.setTentativasFalhas(0); // Reset tentativas falhas na nova solicitacao
        recovery.setExpiraEm(agora.plusMinutes(15));
        recovery.setCriadoEm(agora);
        
        recoveryRepository.save(recovery);

        smsService.enviarSms(cpf, "Seu codigo OTP de recuperacao e: " + otp);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void resetPassword(ResetPasswordRequest request) {
        String cpf = CpfUtils.normalize(request.cpf());
        LocalDateTime agora = LocalDateTime.now();

        PasswordRecovery recovery = recoveryRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recuperacao nao iniciada para este CPF"));

        if (recovery.estaBloqueado(agora)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Recuperacao bloqueada temporariamente");
        }

        if (recovery.estaExpirado(agora)) {
            throw new ResponseStatusException(HttpStatus.GONE, "Codigo OTP expirado");
        }

        if (!recovery.getOtp().equals(request.otp())) {
            int falhas = recovery.getTentativasFalhas() + 1;
            recovery.setTentativasFalhas(falhas);
            if (falhas >= 3) {
                recovery.setBloqueadoAte(agora.plusMinutes(30));
                recovery.setTentativasFalhas(0);
                recoveryRepository.save(recovery);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Codigo OTP incorreto. Limite excedido, recuperacao bloqueada por 30 minutos.");
            }
            recoveryRepository.save(recovery);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Codigo OTP incorreto. Tentativas restantes: " + (3 - falhas));
        }

        // OTP Correto: Atualizar senha do usuário
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        usuario.setSenhaHash(Sha256Util.hash(request.novaSenha()));
        usuarioRepository.save(usuario);

        // Deletar registro de recuperacao para segurança
        recoveryRepository.delete(recovery);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void limparLogsAntigos() {
        LocalDateTime limite = LocalDateTime.now().minusDays(90);
        acessoLogRepository.deleteByTimestampBefore(limite);
    }
}
