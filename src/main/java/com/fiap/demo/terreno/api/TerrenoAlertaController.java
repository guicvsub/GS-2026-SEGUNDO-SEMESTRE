package com.fiap.demo.terreno.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.demo.alertengine.dto.AlertaComposicaoResponse;
import com.fiap.demo.alertengine.dto.GeoMetricasInput;
import com.fiap.demo.alertengine.service.AlertaOrquestracaoService;
import com.fiap.demo.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/terrenos")
@Tag(name = "Terrenos", description = "Alertas via AgroSat.AlertEngine.Api (C#)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TerrenoAlertaController {

    private final AlertaOrquestracaoService alertaOrquestracaoService;

    public TerrenoAlertaController(AlertaOrquestracaoService alertaOrquestracaoService) {
        this.alertaOrquestracaoService = alertaOrquestracaoService;
    }

    @PostMapping("/{id}/alertas/compor")
    @Operation(
            summary = "Compor alerta agricola (C# AlertEngine)",
            description = "Chama AgroSat.AlertEngine.Api e retorna mensagemParaFala para o servico Python TTS."
    )
    public AlertaComposicaoResponse comporAlerta(
            @PathVariable Long id,
            @RequestBody(required = false) GeoMetricasInput geo) {
        return alertaOrquestracaoService.comporAlertaParaTerreno(id, geo);
    }
}
