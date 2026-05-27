package com.fiap.demo.auth.dto;

import java.math.BigDecimal;

public record RegisterRequest(
        String nome,
        String cpf,
        String senha,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal areaCultivo) {
}
