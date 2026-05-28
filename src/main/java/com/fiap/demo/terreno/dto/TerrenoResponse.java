package com.fiap.demo.terreno.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representacao de um terreno cadastrado")
public record TerrenoResponse(
        @Schema(example = "1") Long id,
        @Schema(description = "ID do produtor dono do terreno", example = "2") Long usuarioId,
        @Schema(example = "Lavoura Norte") String nome,
        String descricao,
        @Schema(example = "-15.7000000") BigDecimal latitude,
        @Schema(example = "-47.9000000") BigDecimal longitude,
        @Schema(description = "Area total (ha)", example = "50.00") BigDecimal areaTotalHectares,
        @Schema(description = "Area reserva (ha)", example = "10.00") BigDecimal areaReservaHectares,
        @Schema(description = "Area de cultivo (ha)", example = "35.00") BigDecimal areaCultivoHectares,
        @Schema(example = "true") Boolean emCultivo,
        @Schema(example = "Soja") String culturaAtual,
        @Schema(example = "Argiloso") String tipoSolo,
        @Schema(example = "true") Boolean irrigacaoAtiva,
        @Schema(example = "2026-05-28") LocalDate dataReferencia,
        String observacoes,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
