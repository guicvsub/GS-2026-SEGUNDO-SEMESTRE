using AgroSat.AlertEngine.Api.Models;

namespace AgroSat.AlertEngine.Api.Services;

public interface IAlertCompositionService
{
    AlertaComposicaoResponse Compor(ComporAlertaRequest request);
}
