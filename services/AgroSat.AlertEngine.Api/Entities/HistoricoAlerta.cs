using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AgroSat.AlertEngine.Api.Entities;

[Table("historico_alertas")]
public class HistoricoAlerta
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    public long TerrenoId { get; set; }

    [ForeignKey("TerrenoId")]
    public Terreno Terreno { get; set; } = null!;

    [Required]
    [MaxLength(50)]
    public string Codigo { get; set; } = string.Empty;

    [Required]
    [MaxLength(20)]
    public string Severidade { get; set; } = string.Empty;

    [Required]
    [MaxLength(500)]
    public string MensagemParaFala { get; set; } = string.Empty;

    [Required]
    [MaxLength(1000)]
    public string MensagemTecnica { get; set; } = string.Empty;

    [Required]
    [MaxLength(100)]
    public string AcaoRecomendada { get; set; } = string.Empty;

    [Column(TypeName = "decimal(5,2)")]
    public decimal? NdviMedio { get; set; }

    [Column(TypeName = "decimal(5,2)")]
    public decimal? NdviZonaNorte { get; set; }

    [Column(TypeName = "decimal(5,2)")]
    public decimal? NdviZonaSul { get; set; }

    [Column(TypeName = "decimal(5,2)")]
    public decimal? UmidadeRelativa { get; set; }

    public int? DiasSemImagemSatelite { get; set; }

    [Required]
    public DateTime CriadoEm { get; set; } = DateTime.UtcNow;
}
