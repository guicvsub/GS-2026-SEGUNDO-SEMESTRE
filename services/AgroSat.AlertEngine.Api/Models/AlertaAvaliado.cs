namespace AgroSat.AlertEngine.Api.Models;

internal record AlertaAvaliado(
    int Prioridade,
    string Severidade,
    string Codigo,
    string MensagemParaFala,
    string MensagemTecnica,
    string AcaoRecomendada
);
