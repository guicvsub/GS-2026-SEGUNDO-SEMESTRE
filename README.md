# GS-2026-SEGUNDO-SEMESTRE — AgroShield

Projeto da **Global Solution (FIAP 2026)** — plataforma de agricultura de precisão com autenticação JWT, gestão de usuários e cadastro de terrenos em hectares.

## Motivação do Projeto

O Brasil enfrenta significativas perdas de safra devido à falta de adoção de agricultura de precisão por pequenos produtores rurais. A agricultura tradicional, baseada em métodos empíricos e sem o uso de tecnologias avançadas, resulta em ineficiências, desperdício de recursos e menor produtividade.

## Integrantes

| Nome | RM |
| --- | --- |
| Guilherme Santiago da Silva | RM552321 |
| Gabriel Souza Fiore | RM553710 |
| Gustavo Govea Soares | RM553842 |
| Pedro Henrique Mello Silva Alves | RM554223 |
| Gabriel Borba | RM553187 |

A **AgroShield** surge como uma iniciativa para democratizar o acesso à agricultura de precisão, proporcionando ferramentas acessíveis que permitem:

- **Mapeamento de terrenos** com precisão geográfica
- **Inferência climática** baseada em dados via satélite
- **Alertas agrícolas** personalizados para tomada de decisão
- **Gestão integrada** de propriedades rurais

## Integração no Projeto

Este módulo backend em Java (Spring Boot) é responsável por:

1. **Autenticação e Autorização**
   - Sistema de login seguro com JWT (JSON Web Token)
   - Controle de acesso baseado em papéis (ROLE_USER e ROLE_ADMIN)
   - Recuperação de senha via OTP (One-Time Password)
   - Logs de acesso para auditoria e segurança

2. **Mapeamento de Terrenos**
   - CRUD completo de propriedades rurais
   - Registro de áreas em hectares (total, reserva legal, cultivo)
   - Registro diário de dados de cultivo
   - Validação de áreas e consistência de dados

3. **Integração com Serviços Externos**
   - Comunicação com serviço C# `AgroShield.AlertEngine.Api` para composição de alertas
   - Integração futura com serviço Python TTS (Text-to-Speech) para alertas por voz
   - Arquitetura modular para fácil expansão

4. **Documentação e Monitoramento**
   - Documentação automática via OpenAPI/Swagger
   - Logs estruturados para monitoramento
   - Tarefas agendadas para limpeza de logs (90 dias)

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    AgroShield Ecosystem                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────┐      ┌────────────┐ │
│  │   Frontend   │      │   Backend    │      │  Serviços  │ │
│  │  (Web/Mobile)│◄────►│  (Java/Spring)│◄────►│  Externos   │ │
│  └──────────────┘      └──────────────┘      └────────────┘ │
│         │                      │                      │    │
│         │                      │                      │    │
│         ▼                      ▼                      ▼    │
│  ┌──────────────┐      ┌──────────────┐      ┌────────────┐ │
│  │  UI Dashboard│      │  Auth & Auth │      │  C# Alert  │ │
│  │  Mapas Terrenos│     │  JWT Tokens  │      │  Engine    │ │
│  │  Alertas      │      │  CRUD Terrenos│     │  Python TTS │ │
│  └──────────────┘      └──────────────┘      └────────────┘ │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

Este backend serve como o coração da plataforma, gerenciando autenticação, autorização, dados de terrenos e integração com serviços especializados de alerta e síntese de voz.

---

## Integrantes

| Nome | RM |
| --- | --- |
| Guilherne Santiago da Silva | RM552321 |
| Gabriel Souza Fiore | RM553710 |
| Gustavo Govea Soares | RM553842 |
| Pedro Henrique Mello Silva Alves | RM554223 |
| Gabriel Borba | RM553187 |

## Funcionalidades

- **Autenticacao (RF-AUTH)**: Cadastro, login JWT (8h), recuperacao de senha via OTP/SMS, bloqueio apos tentativas, log de acesso (90 dias)
- **Usuarios**: CRUD com papeis `ROLE_USER` e `ROLE_ADMIN`, estatisticas agregadas de area de cultivo
- **Terrenos**: CRUD de propriedades rurais com areas em hectares, reserva, cultivo ativo e registro diario
- **AlertEngine (C#)**: Composicao de alertas agricolas (`mensagemParaFala`) — consumido pelo AgroShield
- **Seguranca**: SHA-256 para senhas, rotas publicas restritas, JWT em `/api/**`

## Como rodar

1. Tenha **Java 21** e **MySQL** instalados.
2. Crie o banco:
   ```sql
   CREATE DATABASE AgroShield;
   ```
3. Ajuste credenciais em `src/main/resources/application.properties` (padrao: `root` / `0000`).
4. Inicie a aplicacao:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
5. Acesse a documentacao: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Como rodar os testes

```powershell
# Suíte completa
.\mvnw.cmd test -DskipTests=false

# Apenas autenticacao (TC-AUTH-01 a 08)
.\mvnw.cmd test -Dtest=AuthContractTest

# Apenas terrenos (TC-TERRENO-01 a 11)
.\mvnw.cmd test -Dtest=TerrenoContractTest
```

## Documentacao da API

| Recurso | URL |
| --- | --- |
| Swagger UI (Java) | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON (Java) | `http://localhost:8080/v3/api-docs` |
| Swagger UI (C# AlertEngine) | `http://localhost:5050/swagger` |

## Seguranca

- Senhas persistidas com **SHA-256** no campo `senha_hash` (nunca em texto puro).
- Token JWT com expiracao de **8 horas**.
- Papeis:
  - `ROLE_USER` — produtor; gerencia apenas seus terrenos.
  - `ROLE_ADMIN` — acesso ampliado (listagem de usuarios, estatisticas, todos os terrenos).

### Endpoints publicos (sem JWT)

- `POST /auth/register` — Cadastro de produtor
- `POST /auth/login` — Login e emissao de JWT
- `POST /auth/recovery` — Solicitar OTP de recuperacao (SMS simulado)
- `POST /auth/reset-password` — Redefinir senha com OTP
- `GET /swagger-ui.html`, `/v3/api-docs/**` — Documentacao OpenAPI

Todas as demais rotas em `/api/**` exigem header `Authorization: Bearer <seu-token-jwt>`.

## Servico externo — AlertEngine (C#)

Motor de regras **RF-IA parcial** (sem TTS). O Java orquestra; o Python so recebe `mensagemParaFala`.

### Rodar o AlertEngine

```powershell
cd services/AgroShield.AlertEngine.Api
dotnet run
```

Documentacao completa: `services/AgroShield.AlertEngine.Api/README.md`

Configuracao Java: `agrosat.alert-engine.base-url=http://localhost:5050`

## Evidências de Execução

### Console e Request

![Console e Request](evidencias%20de%20execucao/request+console.png)

**Descrição:** Captura de tela demonstrando a execução de um request na API AgroShield, mostrando o console da aplicação com logs de processamento e a resposta da requisição HTTP.

### Logs do Banco de Dados

![Logs do Banco de Dados](evidencias%20de%20execucao/tabela%20do%20banco%20logs.png)

**Descrição:** Captura de tela mostrando os registros no banco de dados MySQL, incluindo tabelas de usuários, terrenos e logs de acesso, demonstrando a persistência dos dados criados pelo DataInitializer.

## Diagramas de Fluxo

Para visualizar os diagramas de fluxo detalhados do sistema, consulte o arquivo [DIAGRAMAS.md](DIAGRAMAS.md), que inclui:

**Diagramas Simplificados (Recomendado para leitura rápida):**
- Diagrama Simplificado - Visão Geral
- Fluxo Principal - Autenticação e Operações
- Fluxo de Terrenos e Alertas
- Fluxo de Recuperação de Senha

**Diagramas Detalhados:**
- Fluxo de Autenticação (Login)
- Fluxo de Recuperação de Senha
- Fluxo de CRUD de Terrenos
- Fluxo de Composição de Alertas
- Fluxo de Segurança JWT
- Arquitetura do Sistema
- Fluxo de Limpeza de Logs (Scheduled)

Os diagramas são renderizados usando Mermaid e podem ser visualizados em editores Markdown que suportam essa sintaxe (como GitHub, GitLab, VS Code com extensão Mermaid).

## Estrutura principal do codigo

```
src/main/java/com/fiap/demo/
├── auth/          # Autenticacao, usuarios, JWT, SMS, logs
├── terreno/       # CRUD de terrenos
├── alertengine/   # Cliente HTTP para o servico C#
└── config/        # Security, JWT filter, OpenAPI, DataInitializer

services/
└── AgroShield.AlertEngine.Api/   # Servico C# de composicao de alertas
```

## Referencia

Documento de requisitos do projeto: `Agro-GS.pdf`.
