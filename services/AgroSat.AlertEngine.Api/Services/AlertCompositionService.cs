using AgroShield.AlertEngine.Api.Models;

namespace AgroShield.AlertEngine.Api.Services;

/// <summary>
/// Motor de regras RF-IA parcial + US-04 (mensagem simples para TTS em Python).
/// </summary>
public class AlertCompositionService : IAlertCompositionService
{
    private const decimal NdviSaudavel = 0.6m;
    private const decimal NdviAtencao = 0.3m;

    public AlertaComposicaoResponse Compor(ComporAlertaRequest request)
    {
        var candidatos = new List<AlertaAvaliado>();

        if (request.Geo != null)
        {
            AvaliarDadosDesatualizados(request, candidatos);
            AvaliarNdviCritico(request, candidatos);
            AvaliarRiscoFungo(request, candidatos);
            AvaliarEstresseHidrico(request, candidatos);
            AvaliarIrrigacaoDesnecessaria(request, candidatos);
        }

        if (!request.EmCultivo && request.AreaCultivoHectares > 0)
        {
            candidatos.Add(new AlertaAvaliado(
                Prioridade: 20,
                Severidade: "BAIXA",
                Codigo: "AREA_CULTIVO_INATIVA",
                MensagemParaFala: $"O terreno {request.Nome} esta com area de cultivo marcada como inativa. Confira o planejamento da safra.",
                MensagemTecnica: "emCultivo=false com areaCultivoHectares>0",
                AcaoRecomendada: "REVISAR_PLANEJAMENTO_SAFRA"
            ));
        }

        if (candidatos.Count == 0)
        {
            return AlertaSaudavel(request);
        }

        var melhor = candidatos.OrderByDescending(c => c.Prioridade).First();
        return new AlertaComposicaoResponse(
            request.TerrenoId,
            melhor.Severidade,
            melhor.Codigo,
            melhor.MensagemParaFala,
            melhor.MensagemTecnica,
            melhor.AcaoRecomendada
        );
    }

    private static void AvaliarDadosDesatualizados(ComporAlertaRequest request, List<AlertaAvaliado> candidatos)
    {
        var dias = request.Geo?.DiasSemImagemSatelite;
        if (dias is null or <= 10)
        {
            return;
        }

        candidatos.Add(new AlertaAvaliado(
            Prioridade: 50,
            Severidade: "MEDIA",
            Codigo: "DADOS_SATELITE_DESATUALIZADOS",
            MensagemParaFala: $"Atencao! Os dados do satelite do terreno {request.Nome} estao desatualizados. Ultima analise ha mais de dez dias.",
            MensagemTecnica: $"diasSemImagemSatelite={dias}",
            AcaoRecomendada: "AGUARDAR_NOVA_IMAGEM_SATELITE"
        ));
    }

    private static void AvaliarNdviCritico(ComporAlertaRequest request, List<AlertaAvaliado> candidatos)
    {
        var ndvi = request.Geo?.NdviMedio;
        if (ndvi is null || ndvi >= NdviAtencao)
        {
            return;
        }

        candidatos.Add(new AlertaAvaliado(
            Prioridade: 80,
            Severidade: "ALTA",
            Codigo: "NDVI_CRITICO",
            MensagemParaFala: $"Atencao! A vegetacao do terreno {request.Nome} esta em nivel critico. Verifique pragas, doencas ou falta de agua.",
            MensagemTecnica: $"NDVI medio={ndvi:F2} (< {NdviAtencao})",
            AcaoRecomendada: "INSPECAO_CAMPO_URGENTE"
        ));
    }

    private static void AvaliarRiscoFungo(ComporAlertaRequest request, List<AlertaAvaliado> candidatos)
    {
        var geo = request.Geo!;
        var ndviNorte = geo.NdviZonaNorte ?? geo.NdviMedio;
        var umidade = geo.UmidadeRelativa;

        if (ndviNorte is null || umidade is null)
        {
            return;
        }

        bool riscoFungo = ndviNorte < NdviAtencao
                          && umidade >= 0.75m
                          && request.IrrigacaoAtiva
                          && request.EmCultivo;

        if (!riscoFungo)
        {
            return;
        }

        candidatos.Add(new AlertaAvaliado(
            Prioridade: 100,
            Severidade: "ALTA",
            Codigo: "RISCO_FUNGO",
            MensagemParaFala: "Atencao! Risco de fungo na area norte. Evite irrigar nos proximos tres dias.",
            MensagemTecnica: $"NDVI norte={ndviNorte:F2}; umidade={umidade:F2}; irrigacao ativa",
            AcaoRecomendada: "SUSPENDER_IRRIGACAO_72H"
        ));
    }

    private static void AvaliarEstresseHidrico(ComporAlertaRequest request, List<AlertaAvaliado> candidatos)
    {
        var geo = request.Geo!;
        var ndviNorte = geo.NdviZonaNorte;
        var ndviSul = geo.NdviZonaSul;

        if (ndviNorte is null || ndviSul is null)
        {
            return;
        }

        if (ndviNorte >= NdviAtencao || ndviSul < NdviSaudavel)
        {
            return;
        }

        candidatos.Add(new AlertaAvaliado(
            Prioridade: 70,
            Severidade: "MEDIA",
            Codigo: "ESTRESSE_HIDRICO",
            MensagemParaFala: "A zona norte do seu terreno apresenta sinais de estresse hidrico. Considere irrigar essa area com moderacao.",
            MensagemTecnica: $"NDVI norte={ndviNorte:F2}; NDVI sul={ndviSul:F2}",
            AcaoRecomendada: "IRRIGAR_ZONA_NORTE_MODERADO"
        ));
    }

    private static void AvaliarIrrigacaoDesnecessaria(ComporAlertaRequest request, List<AlertaAvaliado> candidatos)
    {
        var ndviSul = request.Geo?.NdviZonaSul;
        if (ndviSul is null || ndviSul < NdviSaudavel || !request.IrrigacaoAtiva)
        {
            return;
        }

        candidatos.Add(new AlertaAvaliado(
            Prioridade: 40,
            Severidade: "BAIXA",
            Codigo: "IRRIGACAO_OPCIONAL",
            MensagemParaFala: "A zona sul apresenta boa umidade. A proxima irrigacao pode ser adiada para economizar agua.",
            MensagemTecnica: $"NDVI sul={ndviSul:F2} (>= {NdviSaudavel})",
            AcaoRecomendada: "ADIAR_IRRIGACAO_ZONA_SUL"
        ));
    }

    private static AlertaComposicaoResponse AlertaSaudavel(ComporAlertaRequest request)
    {
        var ndvi = request.Geo?.NdviMedio;
        var ndviTexto = ndvi.HasValue ? $" Indice de vegetacao em {ndvi:F2}." : "";

        return new AlertaComposicaoResponse(
            request.TerrenoId,
            "BAIXA",
            "SAUDAVEL",
            $"Sua lavoura {request.Nome} esta em boa condicao geral.{ndviTexto} Continue o monitoramento.",
            ndvi.HasValue ? $"NDVI medio={ndvi:F2}" : "Sem metricas geo informadas",
            "NENHUMA_ACAO_IMEDIATA"
        );
    }
}
