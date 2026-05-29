namespace AgroSat.AlertEngine.Api.Models;

public class TerrenoResponse
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string? Descricao { get; set; }
    public decimal Latitude { get; set; }
    public decimal Longitude { get; set; }
    public decimal AreaTotalHectares { get; set; }
    public decimal AreaReservaHectares { get; set; }
    public decimal AreaCultivoHectares { get; set; }
    public bool EmCultivo { get; set; }
    public string? CulturaAtual { get; set; }
    public string? TipoSolo { get; set; }
    public bool IrrigacaoAtiva { get; set; }
    public DateTime DataReferencia { get; set; }
    public string? Observacoes { get; set; }
    public DateTime CriadoEm { get; set; }
    public DateTime AtualizadoEm { get; set; }
}
