namespace AgroShield.AlertEngine.Api.Models;

/// <summary>
/// Metricas vindas do servico de geoanalitica (RF-SAT) ou simuladas pelo Java.
/// </summary>
public record GeoMetricasInput(
    decimal? NdviMedio,
    decimal? NdviZonaNorte,
    decimal? NdviZonaSul,
    decimal? UmidadeRelativa,
    int? DiasSemImagemSatelite
);
