using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AgroSat.AlertEngine.Api.Data;
using AgroSat.AlertEngine.Api.Entities;
using AgroSat.AlertEngine.Api.Models;

namespace AgroSat.AlertEngine.Api.Controllers;

[ApiController]
[Route("api/v1/[controller]")]
public class TerrenosController : ControllerBase
{
    private readonly AgroSatDbContext _context;
    private readonly ILogger<TerrenosController> _logger;

    public TerrenosController(AgroSatDbContext context, ILogger<TerrenosController> logger)
    {
        _context = context;
        _logger = logger;
    }

    /// <summary>
    /// Listar todos os terrenos cadastrados
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(List<TerrenoResponse>), StatusCodes.Status200OK)]
    public async Task<ActionResult<List<TerrenoResponse>>> Listar()
    {
        try
        {
            var terrenos = await _context.Terrenos
                .OrderBy(t => t.Nome)
                .ToListAsync();

            var response = terrenos.Select(MapToResponse).ToList();
            return Ok(response);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar terrenos");
            return StatusCode(500, new { message = "Erro interno ao listar terrenos" });
        }
    }

    /// <summary>
    /// Buscar terreno por ID
    /// </summary>
    [HttpGet("{id}")]
    [ProducesResponseType(typeof(TerrenoResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<TerrenoResponse>> BuscarPorId(long id)
    {
        try
        {
            var terreno = await _context.Terrenos.FindAsync(id);

            if (terreno == null)
            {
                return NotFound(new { message = $"Terreno com ID {id} não encontrado" });
            }

            return Ok(MapToResponse(terreno));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar terreno {Id}", id);
            return StatusCode(500, new { message = "Erro interno ao buscar terreno" });
        }
    }

    /// <summary>
    /// Cadastrar novo terreno
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(TerrenoResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<TerrenoResponse>> Criar([FromBody] TerrenoRequest request)
    {
        try
        {
            // Validação de áreas
            if (request.AreaReservaHectares + request.AreaCultivoHectares > request.AreaTotalHectares)
            {
                return BadRequest(new { message = "A soma da área reserva e área de cultivo não pode exceder a área total" });
            }

            var terreno = new Terreno
            {
                Nome = request.Nome,
                Descricao = request.Descricao,
                Latitude = request.Latitude,
                Longitude = request.Longitude,
                AreaTotalHectares = request.AreaTotalHectares,
                AreaReservaHectares = request.AreaReservaHectares,
                AreaCultivoHectares = request.AreaCultivoHectares,
                EmCultivo = request.EmCultivo,
                CulturaAtual = request.CulturaAtual,
                TipoSolo = request.TipoSolo,
                IrrigacaoAtiva = request.IrrigacaoAtiva,
                DataReferencia = request.DataReferencia ?? DateTime.UtcNow,
                Observacoes = request.Observacoes,
                CriadoEm = DateTime.UtcNow,
                AtualizadoEm = DateTime.UtcNow
            };

            _context.Terrenos.Add(terreno);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(BuscarPorId), new { id = terreno.Id }, MapToResponse(terreno));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar terreno");
            return StatusCode(500, new { message = "Erro interno ao criar terreno" });
        }
    }

    /// <summary>
    /// Atualizar terreno existente
    /// </summary>
    [HttpPut("{id}")]
    [ProducesResponseType(typeof(TerrenoResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<TerrenoResponse>> Atualizar(long id, [FromBody] TerrenoRequest request)
    {
        try
        {
            var terreno = await _context.Terrenos.FindAsync(id);

            if (terreno == null)
            {
                return NotFound(new { message = $"Terreno com ID {id} não encontrado" });
            }

            // Validação de áreas
            if (request.AreaReservaHectares + request.AreaCultivoHectares > request.AreaTotalHectares)
            {
                return BadRequest(new { message = "A soma da área reserva e área de cultivo não pode exceder a área total" });
            }

            terreno.Nome = request.Nome;
            terreno.Descricao = request.Descricao;
            terreno.Latitude = request.Latitude;
            terreno.Longitude = request.Longitude;
            terreno.AreaTotalHectares = request.AreaTotalHectares;
            terreno.AreaReservaHectares = request.AreaReservaHectares;
            terreno.AreaCultivoHectares = request.AreaCultivoHectares;
            terreno.EmCultivo = request.EmCultivo;
            terreno.CulturaAtual = request.CulturaAtual;
            terreno.TipoSolo = request.TipoSolo;
            terreno.IrrigacaoAtiva = request.IrrigacaoAtiva;
            terreno.DataReferencia = request.DataReferencia ?? terreno.DataReferencia;
            terreno.Observacoes = request.Observacoes;
            terreno.AtualizadoEm = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return Ok(MapToResponse(terreno));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar terreno {Id}", id);
            return StatusCode(500, new { message = "Erro interno ao atualizar terreno" });
        }
    }

    /// <summary>
    /// Deletar terreno
    /// </summary>
    [HttpDelete("{id}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Deletar(long id)
    {
        try
        {
            var terreno = await _context.Terrenos.FindAsync(id);

            if (terreno == null)
            {
                return NotFound(new { message = $"Terreno com ID {id} não encontrado" });
            }

            _context.Terrenos.Remove(terreno);
            await _context.SaveChangesAsync();

            return NoContent();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao deletar terreno {Id}", id);
            return StatusCode(500, new { message = "Erro interno ao deletar terreno" });
        }
    }

    /// <summary>
    /// Exportar histórico de alertas de um terreno em JSON
    /// </summary>
    [HttpGet("{id}/alertas/exportar")]
    [ProducesResponseType(typeof(List<object>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult> ExportarAlertas(long id)
    {
        try
        {
            var terreno = await _context.Terrenos.FindAsync(id);

            if (terreno == null)
            {
                return NotFound(new { message = $"Terreno com ID {id} não encontrado" });
            }

            var alertas = await _context.HistoricoAlertas
                .Where(h => h.TerrenoId == id)
                .OrderByDescending(h => h.CriadoEm)
                .ToListAsync();

            var exportData = new
            {
                terreno = new
                {
                    id = terreno.Id,
                    nome = terreno.Nome,
                    culturaAtual = terreno.CulturaAtual,
                    areaTotalHectares = terreno.AreaTotalHectares
                },
                totalAlertas = alertas.Count,
                alertas = alertas.Select(a => new
                {
                    id = a.Id,
                    codigo = a.Codigo,
                    severidade = a.Severidade,
                    mensagemParaFala = a.MensagemParaFala,
                    mensagemTecnica = a.MensagemTecnica,
                    acaoRecomendada = a.AcaoRecomendada,
                    metricas = new
                    {
                        ndviMedio = a.NdviMedio,
                        ndviZonaNorte = a.NdviZonaNorte,
                        ndviZonaSul = a.NdviZonaSul,
                        umidadeRelativa = a.UmidadeRelativa,
                        diasSemImagemSatelite = a.DiasSemImagemSatelite
                    },
                    criadoEm = a.CriadoEm
                }).ToList(),
                exportadoEm = DateTime.UtcNow
            };

            return Ok(exportData);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao exportar alertas do terreno {Id}", id);
            return StatusCode(500, new { message = "Erro interno ao exportar alertas" });
        }
    }

    private static TerrenoResponse MapToResponse(Terreno terreno)
    {
        return new TerrenoResponse
        {
            Id = terreno.Id,
            Nome = terreno.Nome,
            Descricao = terreno.Descricao,
            Latitude = terreno.Latitude,
            Longitude = terreno.Longitude,
            AreaTotalHectares = terreno.AreaTotalHectares,
            AreaReservaHectares = terreno.AreaReservaHectares,
            AreaCultivoHectares = terreno.AreaCultivoHectares,
            EmCultivo = terreno.EmCultivo,
            CulturaAtual = terreno.CulturaAtual,
            TipoSolo = terreno.TipoSolo,
            IrrigacaoAtiva = terreno.IrrigacaoAtiva,
            DataReferencia = terreno.DataReferencia,
            Observacoes = terreno.Observacoes,
            CriadoEm = terreno.CriadoEm,
            AtualizadoEm = terreno.AtualizadoEm
        };
    }
}
