package com.fiap.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoveryRequest(
    @NotBlank(message = "O CPF e obrigatorio")
    String cpf
) {}
