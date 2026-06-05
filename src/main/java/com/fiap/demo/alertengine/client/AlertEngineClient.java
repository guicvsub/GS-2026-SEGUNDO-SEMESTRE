package com.fiap.demo.alertengine.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.alertengine.dto.AlertaComposicaoResponse;
import com.fiap.demo.alertengine.dto.ComporAlertaRequest;

@Component
public class AlertEngineClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AlertEngineClient(@Value("${AgroShield.alert-engine.base-url:http://localhost:5050}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AlertaComposicaoResponse compor(ComporAlertaRequest request) {
        try {
            AlertaComposicaoResponse response = restClient.post()
                    .uri("/api/v1/alertas/compor")
                    .body(request)
                    .retrieve()
                    .body(AlertaComposicaoResponse.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, 
                    "Resposta vazia do AlertEngine - verifique se o serviço C# está rodando");
            }
            return response;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (org.springframework.web.client.ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Serviço AgroShield.AlertEngine.Api indisponível ou conexão recusada. " +
                    "Verifique se o serviço C# está rodando em: " + baseUrl);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Erro HTTP do AlertEngine: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Erro interno do AlertEngine: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Erro inesperado ao comunicar com AgroShield.AlertEngine.Api: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }
}
