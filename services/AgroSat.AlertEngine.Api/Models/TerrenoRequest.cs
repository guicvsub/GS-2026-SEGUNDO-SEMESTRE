using System.ComponentModel.DataAnnotations;

namespace AgroSat.AlertEngine.Api.Models;

public class TerrenoRequest
{
    [Required(ErrorMessage = "O nome do terreno é obrigatório")]
    [MaxLength(120, ErrorMessage = "O nome deve ter no máximo 120 caracteres")]
    public string Nome { get; set; } = string.Empty;

    [MaxLength(500, ErrorMessage = "A descrição deve ter no máximo 500 caracteres")]
    public string? Descricao { get; set; }

    [Required(ErrorMessage = "A latitude é obrigatória")]
    [Range(-90, 90, ErrorMessage = "A latitude deve estar entre -90 e 90")]
    public decimal Latitude { get; set; }

    [Required(ErrorMessage = "A longitude é obrigatória")]
    [Range(-180, 180, ErrorMessage = "A longitude deve estar entre -180 e 180")]
    public decimal Longitude { get; set; }

    [Required(ErrorMessage = "A área total é obrigatória")]
    [Range(0.01, double.MaxValue, ErrorMessage = "A área total deve ser maior que zero")]
    public decimal AreaTotalHectares { get; set; }

    [Required(ErrorMessage = "A área de reserva é obrigatória")]
    [Range(0, double.MaxValue, ErrorMessage = "A área de reserva não pode ser negativa")]
    public decimal AreaReservaHectares { get; set; } = 0;

    [Required(ErrorMessage = "A área de cultivo é obrigatória")]
    [Range(0, double.MaxValue, ErrorMessage = "A área de cultivo não pode ser negativa")]
    public decimal AreaCultivoHectares { get; set; } = 0;

    [Required(ErrorMessage = "O indicador emCultivo é obrigatório")]
    public bool EmCultivo { get; set; } = false;

    [MaxLength(80, ErrorMessage = "A cultura atual deve ter no máximo 80 caracteres")]
    public string? CulturaAtual { get; set; }

    [MaxLength(50, ErrorMessage = "O tipo de solo deve ter no máximo 50 caracteres")]
    public string? TipoSolo { get; set; }

    public bool IrrigacaoAtiva { get; set; } = false;

    public DateTime? DataReferencia { get; set; }

    [MaxLength(1000, ErrorMessage = "As observações devem ter no máximo 1000 caracteres")]
    public string? Observacoes { get; set; }
}
