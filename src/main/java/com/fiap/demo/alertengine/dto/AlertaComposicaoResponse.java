package com.fiap.demo.alertengine.dto;

public record AlertaComposicaoResponse(
        Long terrenoId,
        String severidade,
        String codigo,
        String mensagemParaFala,
        String mensagemTecnica,
        String acaoRecomendada) {
}
