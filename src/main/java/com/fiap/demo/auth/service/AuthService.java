package com.fiap.demo.auth.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.dto.AuthResponse;
import com.fiap.demo.auth.dto.LoginRequest;
import com.fiap.demo.auth.dto.RegisterRequest;
import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.repository.UsuarioRepository;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS = 5;

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
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
                .senhaHash(request.senha())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .areaCultivoHectares(request.areaCultivo())
                .build();
        usuarioRepository.save(usuario);
        return new AuthResponse(tokenService.generateToken(cpf), TokenService.TOKEN_EXP_HOURS, true);
    }

    public AuthResponse login(LoginRequest request) {
        String cpf = CpfUtils.normalize(request.cpf());
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

        LocalDateTime agora = LocalDateTime.now();
        if (usuario.estaBloqueado(agora)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Conta bloqueada");
        }

        if (!usuario.getSenhaHash().equals(request.senha())) {
            usuario.registrarFalhaLogin();
            if (usuario.getTentativasFalhasLogin() > MAX_TENTATIVAS) {
                usuario.setBloqueadoAte(agora.plusMinutes(30));
                usuarioRepository.save(usuario);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Conta bloqueada");
            }
            usuarioRepository.save(usuario);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        usuario.resetarTentativasFalhas();
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
        return new AuthResponse(tokenService.generateToken(cpf), TokenService.TOKEN_EXP_HOURS, false);
    }
}
