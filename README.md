# GS-2026-SEGUNDO-SEMESTRE

Projeto da Global Solution (FIAP) com modulo de autenticacao.

## Integrantes

| Nome | RM |
| --- | --- |
| Guilherne Santiago da Silva  | RM552321 |
| Gabriel Souza Fiore | RM553710 |
| Gustavo Govea Soares  | RM553842 |
| Pedro Henrique Mello Silva Alves | RM554223 |
| Gabriel Borba | RM553187 |

## Como rodar

1. Tenha Java 21 e MySQL instalados.
2. Crie o banco:
   - `CREATE DATABASE agrosat;`
3. Confira `src/main/resources/application.properties` (MySQL + `ddl-auto=update`).
4. Rode:
   - `.\mvnw.cmd spring-boot:run`

## Como rodar os testes

1. Execute:
   - `.\mvnw.cmd test -DskipTests=false`
2. Esperado:
   - `BUILD SUCCESS`

## Cenarios AUTH cobertos

- Cadastro valido (`201`)
- CPF duplicado (`409`)
- CPF invalido (`422`)
- Login valido (`200`)
- Senha incorreta (`401`)
- Bloqueio apos tentativas (`429`)
- Rota protegida sem token (`401`)
- Token expirado/invalido (`401`)
