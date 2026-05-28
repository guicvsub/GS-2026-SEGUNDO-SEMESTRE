package com.fiap.demo.auth.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.dto.UsuarioRequest;
import com.fiap.demo.auth.dto.UsuarioResponse;
import com.fiap.demo.auth.dto.EstatisticasCultivoResponse;
import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.model.Role;
import com.fiap.demo.auth.repository.UsuarioRepository;
import com.fiap.demo.config.SecurityUtils;

@Service("usuarioService")
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isOwner(Long id) {
        if (SecurityUtils.isAdmin()) {
            return true;
        }
        String cpf = SecurityUtils.getCurrentCpf();
        if (cpf == null) {
            return false;
        }
        return usuarioRepository.findById(id)
                .map(u -> u.getCpf().equals(cpf))
                .orElse(false);
    }

    @Transactional
    public UsuarioResponse createUsuario(UsuarioRequest request) {
        validarSenhaObrigatoria(request.senha());

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
                .senhaHash(passwordEncoder.encode(request.senha()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .areaCultivoHectares(request.areaCultivo())
                .role(request.role() != null ? request.role() : Role.ROLE_USER)
                .build();

        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getCurrentUsuario() {
        String cpf = SecurityUtils.getCurrentCpf();
        if (cpf == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token nao fornecido");
        }
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponse updateUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        String cpf = CpfUtils.normalize(request.cpf());
        if (!CpfUtils.isValid(cpf)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CPF invalido");
        }

        if (!usuario.getCpf().equals(cpf) && usuarioRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF ja cadastrado por outro usuario");
        }

        usuario.setNome(request.nome());
        usuario.setCpf(cpf);
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }
        usuario.setLatitude(request.latitude());
        usuario.setLongitude(request.longitude());
        usuario.setAreaCultivoHectares(request.areaCultivo());
        if (request.role() != null && SecurityUtils.isAdmin()) {
            usuario.setRole(request.role());
        }

        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    @Transactional
    public void deleteUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public EstatisticasCultivoResponse getEstatisticasCultivo() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        long count = usuarios.size();

        if (count == 0) {
            return new EstatisticasCultivoResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;

        for (Usuario u : usuarios) {
            BigDecimal area = u.getAreaCultivoHectares();
            total = total.add(area);

            if (max == null || area.compareTo(max) > 0) {
                max = area;
            }
            if (min == null || area.compareTo(min) < 0) {
                min = area;
            }
        }

        BigDecimal average = total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);

        return new EstatisticasCultivoResponse(
                total.setScale(2, RoundingMode.HALF_UP),
                average.setScale(2, RoundingMode.HALF_UP),
                count,
                max.setScale(2, RoundingMode.HALF_UP),
                min.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private void validarSenhaObrigatoria(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "A senha e obrigatoria");
        }
        if (senha.length() < 6) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "A senha deve ter no minimo 6 caracteres");
        }
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getLatitude(),
                usuario.getLongitude(),
                usuario.getAreaCultivoHectares(),
                usuario.getRole(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}
