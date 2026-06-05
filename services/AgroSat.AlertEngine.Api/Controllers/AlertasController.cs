using AgroShield.AlertEngine.Api.Models;
using AgroShield.AlertEngine.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace AgroShield.AlertEngine.Api.Controllers;

[ApiController]
[Route("api/v1/alertas")]
public class AlertasController : ControllerBase
{
    private readonly IAlertCompositionService _compositionService;

    public AlertasController(IAlertCompositionService compositionService)
    {
        _compositionService = compositionService;
    }

    /// <summary>
    /// Compoe o alerta de maior prioridade com base no terreno e metricas geo (RF-IA parcial / US-04).
    /// O backend Java envia o resultado para o servico Python TTS (mensagemParaFala).
    /// </summary>
    [HttpPost("compor")]
    [ProducesResponseType(typeof(AlertaComposicaoResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public ActionResult<AlertaComposicaoResponse> Compor([FromBody] ComporAlertaRequest request)
    {
        if (request.TerrenoId <= 0)
        {
            return BadRequest(new { message = "terrenoId deve ser maior que zero" });
        }

        if (string.IsNullOrWhiteSpace(request.Nome))
        {
            return BadRequest(new { message = "nome do terreno e obrigatorio" });
        }

        if (request.Nome.Length > 120)
        {
            return BadRequest(new { message = "nome do terreno deve ter no maximo 120 caracteres" });
        }

        if (request.AreaTotalHectares <= 0)
        {
            return BadRequest(new { message = "areaTotalHectares deve ser maior que zero" });
        }

        if (request.AreaCultivoHectares < 0)
        {
            return BadRequest(new { message = "areaCultivoHectares nao pode ser negativa" });
        }

        // NOTA: A validacao de existencia do terreno no banco de dados deve ser feita
        // pelo servico Java (AlertaOrquestracaoService) antes de chamar este endpoint C#

        var resultado = _compositionService.Compor(request);
        return Ok(resultado);
    }

    [HttpGet("health")]
    public IActionResult Health() => Ok(new { status = "UP", service = "AgroShield.AlertEngine.Api" });
}
