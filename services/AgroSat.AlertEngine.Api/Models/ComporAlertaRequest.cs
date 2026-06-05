namespace AgroShield.AlertEngine.Api.Models;

public record ComporAlertaRequest(
    long TerrenoId,
    string Nome,
    string? CulturaAtual,
    decimal AreaTotalHectares,
    decimal AreaCultivoHectares,
    bool EmCultivo,
    bool IrrigacaoAtiva,
    GeoMetricasInput? Geo
);
