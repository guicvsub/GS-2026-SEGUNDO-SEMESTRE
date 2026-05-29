using AgroSat.AlertEngine.Api.Models;
using AgroSat.AlertEngine.Api.Services;

namespace AgroSat.AlertEngine.Api.Tests;

public class AlertCompositionServiceTests
{
    private readonly AlertCompositionService _service = new();

    [Fact]
    public void Compor_DeveRetornarRiscoFungo_QuandoNdviNorteBaixoEUmidadeAlta()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 5,
            Nome: "Lavoura Norte",
            CulturaAtual: "Soja",
            AreaTotalHectares: 50,
            AreaCultivoHectares: 35,
            EmCultivo: true,
            IrrigacaoAtiva: true,
            Geo: new GeoMetricasInput(0.42m, 0.25m, 0.65m, 0.85m, 2)
        );

        var result = _service.Compor(request);

        Assert.Equal("RISCO_FUNGO", result.Codigo);
        Assert.Equal("ALTA", result.Severidade);
        Assert.Contains("fungo", result.MensagemParaFala, StringComparison.OrdinalIgnoreCase);
        Assert.Equal("SUSPENDER_IRRIGACAO_72H", result.AcaoRecomendada);
    }

    [Fact]
    public void Compor_DeveRetornarSaudavel_QuandoMetricasBoas()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 1,
            Nome: "Lavoura Sul",
            CulturaAtual: "Milho",
            AreaTotalHectares: 40,
            AreaCultivoHectares: 30,
            EmCultivo: true,
            IrrigacaoAtiva: false,
            Geo: new GeoMetricasInput(0.72m, 0.68m, 0.75m, 0.5m, 3)
        );

        var result = _service.Compor(request);

        Assert.Equal("SAUDAVEL", result.Codigo);
        Assert.Equal("BAIXA", result.Severidade);
    }

    [Fact]
    public void Compor_DeveRetornarDadosDesatualizados_QuandoSemImagemMaisDeDezDias()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 2,
            Nome: "Fazenda",
            CulturaAtual: "Soja",
            AreaTotalHectares: 20,
            AreaCultivoHectares: 15,
            EmCultivo: true,
            IrrigacaoAtiva: false,
            Geo: new GeoMetricasInput(0.55m, 0.5m, 0.6m, 0.4m, 15)
        );

        var result = _service.Compor(request);

        Assert.Equal("DADOS_SATELITE_DESATUALIZADOS", result.Codigo);
    }

    [Fact]
    public void Compor_DeveRetornarNdviCritico_QuandoNdviMedioBaixo()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 3,
            Nome: "Lavoura Critica",
            CulturaAtual: "Milho",
            AreaTotalHectares: 30,
            AreaCultivoHectares: 25,
            EmCultivo: true,
            IrrigacaoAtiva: false,
            Geo: new GeoMetricasInput(0.25m, 0.2m, 0.3m, 0.5m, 5)
        );

        var result = _service.Compor(request);

        Assert.Equal("NDVI_CRITICO", result.Codigo);
        Assert.Equal("ALTA", result.Severidade);
        Assert.Contains("critico", result.MensagemParaFala, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void Compor_DeveRetornarEstresseHidrico_QuandoNdviNorteBaixoESulAlto()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 4,
            Nome: "Lavoura Desigual",
            CulturaAtual: "Soja",
            AreaTotalHectares: 40,
            AreaCultivoHectares: 35,
            EmCultivo: true,
            IrrigacaoAtiva: false,
            Geo: new GeoMetricasInput(0.5m, 0.25m, 0.7m, 0.4m, 3)
        );

        var result = _service.Compor(request);

        Assert.Equal("ESTRESSE_HIDRICO", result.Codigo);
        Assert.Equal("MEDIA", result.Severidade);
        Assert.Contains("estresse hidrico", result.MensagemParaFala, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void Compor_DeveRetornarIrrigacaoOpcional_QuandoNdviSulAltoEIrrigacaoAtiva()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 5,
            Nome: "Lavoura Saudavel",
            CulturaAtual: "Trigo",
            AreaTotalHectares: 25,
            AreaCultivoHectares: 20,
            EmCultivo: true,
            IrrigacaoAtiva: true,
            Geo: new GeoMetricasInput(0.7m, 0.65m, 0.75m, 0.5m, 2)
        );

        var result = _service.Compor(request);

        Assert.Equal("IRRIGACAO_OPCIONAL", result.Codigo);
        Assert.Equal("BAIXA", result.Severidade);
        Assert.Contains("adiar", result.MensagemParaFala, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void Compor_DeveRetornarAreaCultivoInativa_QuandoEmCultivoFalseEAreaCultivoMaiorQueZero()
    {
        var request = new ComporAlertaRequest(
            TerrenoId: 6,
            Nome: "Terreno Parado",
            CulturaAtual: null,
            AreaTotalHectares: 50,
            AreaCultivoHectares: 30,
            EmCultivo: false,
            IrrigacaoAtiva: false,
            Geo: null
        );

        var result = _service.Compor(request);

        Assert.Equal("AREA_CULTIVO_INATIVA", result.Codigo);
        Assert.Equal("BAIXA", result.Severidade);
        Assert.Contains("inativa", result.MensagemParaFala, StringComparison.OrdinalIgnoreCase);
    }
}
