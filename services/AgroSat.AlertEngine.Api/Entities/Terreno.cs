using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AgroShield.AlertEngine.Api.Entities;

[Table("terrenos")]
public class Terreno
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    [MaxLength(120)]
    public string Nome { get; set; } = string.Empty;

    [MaxLength(500)]
    public string? Descricao { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,7)")]
    public decimal Latitude { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,7)")]
    public decimal Longitude { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal AreaTotalHectares { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal AreaReservaHectares { get; set; } = 0;

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal AreaCultivoHectares { get; set; } = 0;

    [Required]
    public bool EmCultivo { get; set; } = false;

    [MaxLength(80)]
    public string? CulturaAtual { get; set; }

    [MaxLength(50)]
    public string? TipoSolo { get; set; }

    [Required]
    public bool IrrigacaoAtiva { get; set; } = false;

    [Required]
    public DateTime DataReferencia { get; set; } = DateTime.UtcNow;

    [MaxLength(1000)]
    public string? Observacoes { get; set; }

    [Required]
    public DateTime CriadoEm { get; set; } = DateTime.UtcNow;

    [Required]
    public DateTime AtualizadoEm { get; set; } = DateTime.UtcNow;

    public ICollection<HistoricoAlerta> HistoricoAlertas { get; set; } = new List<HistoricoAlerta>();
}
