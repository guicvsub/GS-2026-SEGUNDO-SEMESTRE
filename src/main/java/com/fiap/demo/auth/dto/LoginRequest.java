package com.fiap.demo.auth.dto;

public record LoginRequest(
        String cpf,
        String senha) {
}
