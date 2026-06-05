namespace AgroShield.AlertEngine.Api.Models;

public record AlertaComposicaoResponse(
    long TerrenoId,
    string Severidade,
    string Codigo,
    string MensagemParaFala,
    string MensagemTecnica,
    string AcaoRecomendada
);
