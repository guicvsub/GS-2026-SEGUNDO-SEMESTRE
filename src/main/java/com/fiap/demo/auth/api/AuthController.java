package com.fiap.demo.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.demo.auth.dto.AuthResponse;
import com.fiap.demo.auth.dto.LoginRequest;
import com.fiap.demo.auth.dto.RegisterRequest;
import com.fiap.demo.auth.dto.RecoveryRequest;
import com.fiap.demo.auth.dto.ResetPasswordRequest;
import com.fiap.demo.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Autenticacao", description = "Endpoints publicos de cadastro e login (RF-AUTH)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "TC-AUTH-01 — Cadastrar produtor", description = "Publico. Retorna JWT e envia SMS de boas-vindas. Senha salva com BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "TC-AUTH-02 — CPF duplicado"),
            @ApiResponse(responseCode = "422", description = "TC-AUTH-03 — CPF invalido")
    })
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "TC-AUTH-04 — Login", description = "Publico. Retorna JWT com expiracao de 8 horas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login OK",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "TC-AUTH-05 — Senha incorreta"),
            @ApiResponse(responseCode = "429", description = "TC-AUTH-06 — Conta bloqueada apos 5 tentativas")
    })
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/recovery")
    @Operation(summary = "RF-AUTH-03 — Solicitar recuperacao de senha", description = "Publico. Envia OTP de 6 digitos por SMS (valido 15 min).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP enviado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "429", description = "Fluxo bloqueado temporariamente")
    })
    public void recovery(@RequestBody @Valid RecoveryRequest request) {
        authService.requestRecovery(request);
    }

    @PostMapping("/auth/reset-password")
    @Operation(summary = "RF-AUTH-03 — Redefinir senha com OTP", description = "Publico. Nova senha persistida com BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "410", description = "OTP expirado"),
            @ApiResponse(responseCode = "422", description = "OTP incorreto"),
            @ApiResponse(responseCode = "429", description = "Bloqueio apos 3 tentativas incorretas")
    })
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}
