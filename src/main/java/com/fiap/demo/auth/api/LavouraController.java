package com.fiap.demo.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.service.TokenService;
import com.fiap.demo.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Lavoura", description = "Rota de exemplo para validar JWT")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class LavouraController {

    private final TokenService tokenService;

    public LavouraController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/api/lavoura")
    @Operation(summary = "TC-AUTH-07/08 — Acesso protegido", description = "Exige Bearer JWT valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso liberado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou invalido")
    })
    public String lavoura(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token nao fornecido");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
        }
        String token = authorization.substring(7);
        if (tokenService.isExpired(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
        }
        return "acesso liberado";
    }
}
