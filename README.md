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

| Modulo | Descricao |
| --- | --- |
| **Autenticacao (RF-AUTH)** | Cadastro, login JWT (8h), recuperacao de senha via OTP/SMS, bloqueio apos tentativas, log de acesso (90 dias) |
| **Usuarios** | CRUD com papeis `ROLE_USER` e `ROLE_ADMIN`, estatisticas agregadas de area de cultivo |
| **Terrenos** | CRUD de propriedades rurais com areas em hectares, reserva, cultivo ativo e registro diario |
| **Documentacao** | SpringDoc / Swagger UI com cenarios de teste (TC-AUTH e TC-TERRENO) |
| **Seguranca** | BCrypt, rotas publicas restritas, JWT em `/api/**` |

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

Esperado: `BUILD SUCCESS`.

## Documentacao da API (SpringDoc)

| Recurso | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

No Swagger, use **Authorize** com `Bearer <token>` obtido em `POST /auth/login` ou `POST /auth/register`.

## Seguranca

- Senhas persistidas com **BCrypt** no campo `senha_hash` (nunca em texto puro).
- Token JWT com expiracao de **8 horas**.
- Papeis:
  - `ROLE_USER` — produtor; gerencia apenas seus terrenos.
  - `ROLE_ADMIN` — acesso ampliado (listagem de usuarios, estatisticas, todos os terrenos).

### Endpoints publicos (sem JWT)

| Metodo | Rota | Descricao |
| --- | --- | --- |
| POST | `/auth/register` | Cadastro de produtor |
| POST | `/auth/login` | Login e emissao de JWT |
| POST | `/auth/recovery` | Solicitar OTP de recuperacao (SMS simulado) |
| POST | `/auth/reset-password` | Redefinir senha com OTP |
| GET | `/swagger-ui.html`, `/v3/api-docs/**` | Documentacao OpenAPI |

Todas as demais rotas em `/api/**` exigem header:

```http
Authorization: Bearer <seu-token-jwt>
```

## API — Autenticacao

| Metodo | Rota | HTTP esperado | Observacao |
| --- | --- | --- | --- |
| POST | `/auth/register` | 201 | TC-AUTH-01; retorna token + SMS |
| POST | `/auth/login` | 200 | TC-AUTH-04; JWT 8h |
| POST | `/auth/recovery` | 200 | RF-AUTH-03; OTP 6 digitos, 15 min |
| POST | `/auth/reset-password` | 200 | Nova senha com BCrypt |

## API — Usuarios (`/api/usuarios`)

| Metodo | Rota | Autorizacao | Descricao |
| --- | --- | --- | --- |
| POST | `/api/usuarios` | ADMIN | Criar usuario |
| GET | `/api/usuarios` | ADMIN | Listar todos |
| GET | `/api/usuarios/me` | Autenticado | Perfil do usuario logado |
| GET | `/api/usuarios/{id}` | ADMIN ou dono | Buscar por id |
| PUT | `/api/usuarios/{id}` | ADMIN ou dono | Atualizar (senha opcional, re-hash BCrypt) |
| DELETE | `/api/usuarios/{id}` | ADMIN | Remover usuario |
| GET | `/api/usuarios/estatisticas-cultivo` | ADMIN | Totais/medias de area de cultivo |

## API — Terrenos (`/api/terrenos`)

CRUD completo vinculado ao produtor autenticado.

| Metodo | Rota | HTTP | Caso de teste |
| --- | --- | --- | --- |
| POST | `/api/terrenos` | 201 | TC-TERRENO-01 |
| GET | `/api/terrenos` | 200 | TC-TERRENO-02 / TC-TERRENO-11 (admin) |
| GET | `/api/terrenos/{id}` | 200 | TC-TERRENO-03 |
| PUT | `/api/terrenos/{id}` | 200 | TC-TERRENO-04 |
| DELETE | `/api/terrenos/{id}` | 204 | TC-TERRENO-05 |

### Campos do terreno (JSON)

| Campo | Tipo | Descricao |
| --- | --- | --- |
| `nome` | string | Nome do talhao/propriedade |
| `descricao` | string | Descricao opcional |
| `latitude` / `longitude` | number | Coordenadas WGS84 |
| `areaTotalHectares` | number | Area total (ha) |
| `areaReservaHectares` | number | Area de reserva legal (ha) |
| `areaCultivoHectares` | number | Area plantada/em cultivo (ha) |
| `emCultivo` | boolean | Indica se a area de cultivo esta ativa |
| `culturaAtual` | string | Ex.: Soja, Milho |
| `tipoSolo` | string | Ex.: Argiloso |
| `irrigacaoAtiva` | boolean | Irrigacao em uso |
| `dataReferencia` | date | Registro diario (`YYYY-MM-DD`) |
| `observacoes` | string | Notas de campo / monitoramento |

**Regra de negocio:** `areaReservaHectares + areaCultivoHectares` nao pode exceder `areaTotalHectares` (HTTP 422).

### Exemplo de payload (criar terreno)

```json
{
  "nome": "Lavoura Norte",
  "descricao": "Talhao principal",
  "latitude": -15.7,
  "longitude": -47.9,
  "areaTotalHectares": 50.00,
  "areaReservaHectares": 10.00,
  "areaCultivoHectares": 35.00,
  "emCultivo": true,
  "culturaAtual": "Soja",
  "tipoSolo": "Argiloso",
  "irrigacaoAtiva": true,
  "dataReferencia": "2026-05-28",
  "observacoes": "Monitoramento satelital ativo"
}
```

## API — Lavoura (exemplo JWT)

| Metodo | Rota | Descricao |
| --- | --- | --- |
| GET | `/api/lavoura` | Rota protegida de exemplo (TC-AUTH-07/08) |

## Collections Postman

| Arquivo | Conteudo |
| --- | --- |
| `postman/AGROSAT_AUTH.postman_collection.json` | Cenarios TC-AUTH-01 a 08 |
| `postman/AGROSAT_USUARIO_CRUD.postman_collection.json` | CRUD de usuarios |
| `postman/AGROSAT_TERRENO_CRUD.postman_collection.json` | CRUD de terrenos |

## Cenarios de teste automatizados

### AUTH — `AuthContractTest` (TC-AUTH-01 a 08)

- Cadastro valido (`201`), CPF duplicado (`409`), CPF invalido (`422`)
- Login valido (`200`), senha incorreta (`401`), bloqueio apos 5 tentativas (`429`)
- Rota protegida sem token (`401`), token invalido (`401`)

### TERRENO — `TerrenoContractTest` (TC-TERRENO-01 a 11)

- CRUD completo (`201` / `200` / `204`)
- Sem token (`401`), areas invalidas (`422`), isolamento entre produtores (`404`)
- Admin lista terrenos de todos os usuarios (`200`)

## Estrutura principal do codigo

```
src/main/java/com/fiap/demo/
├── auth/          # Autenticacao, usuarios, JWT, SMS, logs
├── terreno/       # CRUD de terrenos
└── config/        # Security, JWT filter, OpenAPI
```

## Referencia

Documento de requisitos do projeto: `Agro-GS.pdf`.
