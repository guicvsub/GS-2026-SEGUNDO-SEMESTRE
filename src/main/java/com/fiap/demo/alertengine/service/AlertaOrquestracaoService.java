package com.fiap.demo.alertengine.service;

import org.springframework.stereotype.Service;

import com.fiap.demo.alertengine.client.AlertEngineClient;
import com.fiap.demo.alertengine.dto.AlertaComposicaoResponse;
import com.fiap.demo.alertengine.dto.ComporAlertaRequest;
import com.fiap.demo.alertengine.dto.GeoMetricasInput;
import com.fiap.demo.terreno.model.Terreno;
import com.fiap.demo.terreno.service.TerrenoService;

@Service
public class AlertaOrquestracaoService {

    private final TerrenoService terrenoService;
    private final AlertEngineClient alertEngineClient;

    public AlertaOrquestracaoService(TerrenoService terrenoService, AlertEngineClient alertEngineClient) {
        this.terrenoService = terrenoService;
        this.alertEngineClient = alertEngineClient;
    }

    /**
     * Busca terreno, monta payload e chama o servico C# AlertEngine.
     * Proximo passo (Python TTS): enviar response.mensagemParaFala() para POST /speak.
     */
    public AlertaComposicaoResponse comporAlertaParaTerreno(Long terrenoId, GeoMetricasInput geo) {
        Terreno terreno = terrenoService.findEntityById(terrenoId);

        ComporAlertaRequest request = new ComporAlertaRequest(
                terreno.getId(),
                terreno.getNome(),
                terreno.getCulturaAtual(),
                terreno.getAreaTotalHectares(),
                terreno.getAreaCultivoHectares(),
                Boolean.TRUE.equals(terreno.getEmCultivo()),
                Boolean.TRUE.equals(terreno.getIrrigacaoAtiva()),
                geo
        );

        return alertEngineClient.compor(request);
    }
}
