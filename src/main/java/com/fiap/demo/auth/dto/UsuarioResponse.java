package com.fiap.demo.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fiap.demo.auth.model.Role;

public record UsuarioResponse(
    Long id,
    String nome,
    String cpf,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal areaCultivo,
    Role role,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {}
