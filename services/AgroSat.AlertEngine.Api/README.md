# AgroSat.AlertEngine.Api

Servico **backend** em C# (.NET 8) para composicao de alertas agricolas e gerenciamento de terrenos (RF-IA parcial, US-04).

- **Entrada:** dados do terreno + metricas geo (NDVI, umidade, etc.)
- **Saida:** JSON com `mensagemParaFala` para o servico **Python TTS**
- **Consumidor:** API Spring Boot (Java) — orquestrador
- **Banco de dados:** MySQL com Entity Framework Core

## Funcionalidades

### 1. Composicao de Alertas Agricolas
- Endpoint: `POST /api/v1/alertas/compor`
- Avalia metricas NDVI, umidade, irrigacao e dias sem imagem satelite
- Retorna alerta priorizado com mensagem para TTS

### 2. CRUD de Terrenos
- Endpoint: `GET /api/v1/terrenos` - Listar todos
- Endpoint: `GET /api/v1/terrenos/{id}` - Buscar por ID
- Endpoint: `POST /api/v1/terrenos` - Criar novo
- Endpoint: `PUT /api/v1/terrenos/{id}` - Atualizar
- Endpoint: `DELETE /api/v1/terrenos/{id}` - Deletar

### 3. Exportacao de Historico de Alertas
- Endpoint: `GET /api/v1/terrenos/{id}/alertas/exportar`
- Exporta historico de alertas de um terreno em formato JSON

## Rodar

```powershell
cd services/AgroSat.AlertEngine.Api
dotnet restore
dotnet run
```

- API: `http://localhost:5050`
- Swagger: `http://localhost:5050/swagger`

## Configuracao do Banco de Dados

O projeto utiliza MySQL com Entity Framework Core. Configure a connection string em `appsettings.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=agrosat_csharp;User=root;Password=0000;SslMode=None;AllowPublicKeyRetrieval=True"
  }
}
```

### Criar e Aplicar Migration

```powershell
# Criar migration inicial
dotnet ef migrations add InitialCreate

# Aplicar migration
dotnet ef database update
```

## Endpoint principal: Composicao de Alertas

`POST /api/v1/alertas/compor`

### Exemplo — Risco de fungo (TC alinhado ao PDF)

```json
{
  "terrenoId": 5,
  "nome": "Lavoura Norte",
  "culturaAtual": "Soja",
  "areaTotalHectares": 50,
  "areaCultivoHectares": 35,
  "emCultivo": true,
  "irrigacaoAtiva": true,
  "geo": {
    "ndviMedio": 0.42,
    "ndviZonaNorte": 0.25,
    "ndviZonaSul": 0.65,
    "umidadeRelativa": 0.85,
    "diasSemImagemSatelite": 2
  }
}
```

### Resposta

```json
{
  "terrenoId": 5,
  "severidade": "ALTA",
  "codigo": "RISCO_FUNGO",
  "mensagemParaFala": "Atencao! Risco de fungo na area norte. Evite irrigar nos proximos tres dias.",
  "mensagemTecnica": "NDVI norte=0.25; umidade=0.85; irrigacao ativa",
  "acaoRecomendada": "SUSPENDER_IRRIGACAO_72H"
}
```

## Regras implementadas

| Codigo | Severidade | Condicao resumida |
|--------|------------|-------------------|
| `RISCO_FUNGO` | ALTA | NDVI norte baixo + umidade alta + irrigacao ativa |
| `NDVI_CRITICO` | ALTA | NDVI medio &lt; 0,3 |
| `ESTRESSE_HIDRICO` | MEDIA | NDVI norte baixo, sul ok |
| `DADOS_SATELITE_DESATUALIZADOS` | MEDIA | Sem imagem &gt; 10 dias |
| `IRRIGACAO_OPCIONAL` | BAIXA | Zona sul com NDVI alto |
| `AREA_CULTIVO_INATIVA` | BAIXA | emCultivo false com area &gt; 0 |
| `SAUDAVEL` | BAIXA | Nenhuma regra critica disparada |

## Fluxo com Java e Python

```text
Java (Spring)  --POST /compor-->  C# AlertEngine
Java           --POST /speak-->   Python TTS  (mensagemParaFala -> MP3)
Java           --salva-->         MySQL (alerta + urlAudio)
```

## Decisoes Tecnicas

### Tecnologia
- **Framework:** .NET 8 Web API
- **ORM:** Entity Framework Core 8.0.10
- **Provider MySQL:** Pomelo.EntityFrameworkCore.MySql 8.0.2
- **Documentacao:** Swashbuckle.AspNetCore 6.6.2

### Arquitetura
- **Padrao:** Controller-Service-Repository (simplificado)
- **Injecao de Dependencia:** DI nativa do .NET
- **Validacao:** Data Annotations em DTOs
- **Tratamento de Erros:** try-catch com logging estruturado

### Entidades de Dominio

#### Terreno
- Representa uma propriedade rural ou talhao
- Campos: nome, descricao, coordenadas, areas, cultura, tipo de solo, irrigacao
- Relacionamento: 1:N com HistoricoAlerta

#### HistoricoAlerta
- Registra todos os alertas gerados para um terreno
- Campos: codigo, severidade, mensagens, metricas no momento do alerta
- Relacionamento: N:1 com Terreno

### Design de API
- **RESTful:** Verbos HTTP adequados (GET, POST, PUT, DELETE)
- **Status Codes:** 200, 201, 204, 400, 404, 500
- **Content-Type:** application/json
- **Versionamento:** /api/v1/

### Segurança
- **Nota:** Este servico e interno e deve ser protegido por firewall ou rede privada
- **Futuro:** Adicionar autenticacao JWT se necessario para acesso externo

### Performance
- **Indices:** Configurados em campos frequentemente consultados (nome, terrenoId, codigo, criadoEm)
- **Lazy Loading:** Nao utilizado (eager loading para simplicidade)
- **Connection Pooling:** Gerenciado automaticamente pelo EF Core

### Testes
- **Unitarios:** AlertCompositionServiceTests.cs (7 testes)
- **Cobertura:** 100% dos cenarios de alerta
- **Execucao:** `dotnet test`

## Testes

```powershell
dotnet test
```

## Estrutura do Projeto

```
AgroSat.AlertEngine.Api/
├── Controllers/
│   ├── AlertasController.cs       # Composicao de alertas
│   └── TerrenosController.cs      # CRUD de terrenos
├── Data/
│   └── AgroSatDbContext.cs        # DbContext EF Core
├── Entities/
│   ├── Terreno.cs                 # Entidade Terreno
│   └── HistoricoAlerta.cs         # Entidade HistoricoAlerta
├── Models/
│   ├── ComporAlertaRequest.cs     # DTO para composicao
│   ├── AlertaComposicaoResponse.cs # DTO de resposta
│   ├── GeoMetricasInput.cs        # DTO de metricas
│   ├── TerrenoRequest.cs          # DTO para CRUD
│   └── TerrenoResponse.cs         # DTO de resposta CRUD
├── Services/
│   ├── IAlertCompositionService.cs
│   └── AlertCompositionService.cs # Logica de composicao
├── Program.cs                      # Configuracao da aplicacao
├── appsettings.json               # Configuracoes
└── AgroSat.AlertEngine.Api.csproj # Dependencias
```
