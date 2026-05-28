package com.fiap.demo.terreno.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para criar ou atualizar um terreno (hectares)")
public record TerrenoRequest(
        @Schema(description = "Nome do talhao ou propriedade", example = "Lavoura Norte")
        @NotBlank(message = "O nome do terreno e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @Schema(description = "Descricao opcional", example = "Talhao principal com irrigacao")
        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String descricao,

        @Schema(description = "Latitude WGS84", example = "-15.7000000")
        @NotNull(message = "A latitude e obrigatoria")
        BigDecimal latitude,

        @Schema(description = "Longitude WGS84", example = "-47.9000000")
        @NotNull(message = "A longitude e obrigatoria")
        BigDecimal longitude,

        @Schema(description = "Area total da propriedade em hectares", example = "50.00")
        @NotNull(message = "A area total e obrigatoria")
        @DecimalMin(value = "0.01", message = "A area total deve ser maior que zero")
        BigDecimal areaTotalHectares,

        @Schema(description = "Area de reserva legal ou preservacao (ha)", example = "10.00")
        @NotNull(message = "A area reserva e obrigatoria")
        @DecimalMin(value = "0.00", message = "A area reserva nao pode ser negativa")
        BigDecimal areaReservaHectares,

        @Schema(description = "Area plantada ou em cultivo (ha)", example = "35.00")
        @NotNull(message = "A area de cultivo e obrigatoria")
        @DecimalMin(value = "0.00", message = "A area de cultivo nao pode ser negativa")
        BigDecimal areaCultivoHectares,

        @Schema(description = "Indica se a area de cultivo esta ativa", example = "true")
        @NotNull(message = "O indicador emCultivo e obrigatorio")
        Boolean emCultivo,

        @Schema(description = "Cultura atual na area", example = "Soja")
        @Size(max = 80, message = "A cultura atual deve ter no maximo 80 caracteres")
        String culturaAtual,

        @Schema(description = "Classificacao do solo", example = "Argiloso")
        @Size(max = 50, message = "O tipo de solo deve ter no maximo 50 caracteres")
        String tipoSolo,

        @Schema(description = "Sistema de irrigacao ativo", example = "true")
        Boolean irrigacaoAtiva,

        @Schema(description = "Data de referencia do registro diario", example = "2026-05-28")
        LocalDate dataReferencia,

        @Schema(description = "Observacoes de campo ou monitoramento", example = "Monitoramento satelital ativo")
        @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
        String observacoes
) {}
