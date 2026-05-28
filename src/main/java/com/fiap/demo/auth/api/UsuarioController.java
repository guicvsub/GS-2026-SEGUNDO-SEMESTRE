package com.fiap.demo.auth.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.demo.auth.dto.UsuarioRequest;
import com.fiap.demo.auth.dto.UsuarioResponse;
import com.fiap.demo.auth.dto.EstatisticasCultivoResponse;
import com.fiap.demo.auth.service.UsuarioService;
import com.fiap.demo.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios — requer JWT; operacoes administrativas exigem ROLE_ADMIN")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar usuario (ADMIN)", description = "Senha armazenada com BCrypt no banco.")
    public UsuarioResponse create(@RequestBody @Valid UsuarioRequest request) {
        return usuarioService.createUsuario(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os usuarios (ADMIN)")
    public List<UsuarioResponse> list() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Perfil do usuario autenticado")
    public UsuarioResponse me() {
        return usuarioService.getCurrentUsuario();
    }

    @GetMapping("/estatisticas-cultivo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estatisticas agregadas de area de cultivo (ADMIN)")
    public EstatisticasCultivoResponse getEstatisticas() {
        return usuarioService.getEstatisticasCultivo();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id)")
    @Operation(summary = "Buscar usuario por id")
    public UsuarioResponse get(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.isOwner(#id)")
    @Operation(summary = "Atualizar usuario", description = "Se senha informada, re-hash com BCrypt.")
    public UsuarioResponse update(@PathVariable Long id, @RequestBody @Valid UsuarioRequest request) {
        return usuarioService.updateUsuario(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar usuario (ADMIN)")
    public void delete(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
    }
}
