package com.fiap.demo.auth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/api/lavoura")
    @Operation(summary = "TC-AUTH-07/08 — Acesso protegido", description = "Exige Bearer JWT valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso liberado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou invalido")
    })
    public String lavoura() {
        return "acesso liberado";
    }
}
