package com.fiap.demo.auth.dto;

import java.math.BigDecimal;
import com.fiap.demo.auth.model.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
    String nome,

    @NotBlank(message = "O CPF e obrigatorio")
    String cpf,

    @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
    String senha,

    @NotNull(message = "A latitude e obrigatoria")
    BigDecimal latitude,

    @NotNull(message = "A longitude e obrigatoria")
    BigDecimal longitude,

    @NotNull(message = "A area de cultivo e obrigatoria")
    @DecimalMin(value = "0.01", message = "A area de cultivo deve ser maior que zero")
    BigDecimal areaCultivo,

    Role role
) {}
