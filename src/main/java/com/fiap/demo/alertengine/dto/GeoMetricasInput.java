package com.fiap.demo.alertengine.dto;

import java.math.BigDecimal;

public record GeoMetricasInput(
        BigDecimal ndviMedio,
        BigDecimal ndviZonaNorte,
        BigDecimal ndviZonaSul,
        BigDecimal umidadeRelativa,
        Integer diasSemImagemSatelite) {
}
