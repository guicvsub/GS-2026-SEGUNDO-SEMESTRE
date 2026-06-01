# GS-2026-SEGUNDO-SEMESTRE — AGROSAT

Projeto da **Global Solution (FIAP 2026)** — plataforma de agricultura de precisão com autenticação JWT, gestão de usuários e cadastro de terrenos em hectares.

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
- **AlertEngine (C#)**: Composicao de alertas agricolas (`mensagemParaFala`) — consumido pelo Java
- **Seguranca**: SHA-256 para senhas, rotas publicas restritas, JWT em `/api/**`

## Como rodar

1. Tenha **Java 21** e **MySQL** instalados.
2. Crie o banco:
   ```sql
   CREATE DATABASE agrosat;
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
cd services/AgroSat.AlertEngine.Api
dotnet run
```

Documentacao completa: `services/AgroSat.AlertEngine.Api/README.md`

Configuracao Java: `agrosat.alert-engine.base-url=http://localhost:5050`

## Collections Postman

| Arquivo | Conteudo |
| --- | --- |
| `postman/AGROSAT_JAVA.postman_collection.json` | Coleção completa com todos os endpoints (Autenticacao, Terrenos, Alertas, Usuarios) |

**Como usar:**
1. Abra o Postman
2. Clique em "Import" e selecione o arquivo `postman/AGROSAT_JAVA.postman_collection.json`
3. Execute `POST /auth/login (Usuario Comum)` para obter o token JWT
4. Atualize a variável `{{token}}` na coleção Postman
5. Execute os demais requests

**Nota:** O sistema popula automaticamente dados de teste ao iniciar. Veja detalhes no Swagger.

## Estrutura principal do codigo

```
src/main/java/com/fiap/demo/
├── auth/          # Autenticacao, usuarios, JWT, SMS, logs
├── terreno/       # CRUD de terrenos
├── alertengine/   # Cliente HTTP para o servico C#
└── config/        # Security, JWT filter, OpenAPI, DataInitializer

services/
└── AgroSat.AlertEngine.Api/   # Servico C# de composicao de alertas
```

## Referencia

Documento de requisitos do projeto: `Agro-GS.pdf`.
