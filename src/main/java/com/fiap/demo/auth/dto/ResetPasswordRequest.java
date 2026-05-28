package com.fiap.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "O CPF e obrigatorio")
    String cpf,

    @NotBlank(message = "O OTP e obrigatorio")
    @Size(min = 6, max = 6, message = "O OTP deve ter 6 digitos")
    String otp,

    @NotBlank(message = "A nova senha e obrigatoria")
    @Size(min = 6, message = "A nova senha deve ter no minimo 6 caracteres")
    String novaSenha
) {}
