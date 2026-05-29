package com.fiap.demo.alertengine.dto;

import java.math.BigDecimal;

public record ComporAlertaRequest(
        Long terrenoId,
        String nome,
        String culturaAtual,
        BigDecimal areaTotalHectares,
        BigDecimal areaCultivoHectares,
        boolean emCultivo,
        boolean irrigacaoAtiva,
        GeoMetricasInput geo) {
}
