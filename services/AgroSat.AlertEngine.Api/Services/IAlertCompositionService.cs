using AgroShield.AlertEngine.Api.Models;

namespace AgroShield.AlertEngine.Api.Services;

public interface IAlertCompositionService
{
    AlertaComposicaoResponse Compor(ComporAlertaRequest request);
}
