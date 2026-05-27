package com.fiap.demo.auth.dto;

public record AuthResponse(
        String token,
        int expiresInHours,
        boolean smsSent) {
}
