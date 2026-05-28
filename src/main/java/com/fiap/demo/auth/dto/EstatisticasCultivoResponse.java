package com.fiap.demo.auth.dto;

import java.math.BigDecimal;

public record EstatisticasCultivoResponse(
    BigDecimal totalAreaCultivada,
    BigDecimal mediaAreaCultivada,
    long totalUsuarios,
    BigDecimal maiorAreaCultivada,
    BigDecimal menorAreaCultivada
) {}
