# Diagramas de Fluxo - AgroShield

## Diagrama Simplificado - Visão Geral

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activityFontColor #333
skinparam activityBorderColor #333
skinparam activityBackgroundColor #E1F5FF

skinparam arrowColor #333
skinparam arrowThickness 2

skinparam participantBorderColor #333
skinparam participantBackgroundColor #FFF4E1

skinparam databaseBorderColor #333
skinparam databaseBackgroundColor #E8F5E9

skinparam externalBorderColor #333
skinparam externalBackgroundColor #F3E5F5

skinparam cloudBorderColor #333
skinparam cloudBackgroundColor #FCE4EC

actor "Cliente" as Client
participant "AuthController" as Auth
participant "API Protegida" as API
participant "TerrenoController" as Terreno
participant "UsuarioController" as Usuario
participant "AlertEngine C#" as CSharp
participant "Python TTS" as TTS

Client -> Auth : 1. Registro/Login
Auth -> API : 3. JWT 8h
Client -> API : 2. JWT Token
API -> Terreno : 4. CRUD Terrenos
API -> Usuario : 5. CRUD Usuarios
Terreno -> CSharp : 6. Alertas
CSharp -> TTS : 7. mensagemParaFala

@enduml
```

## Fluxo Principal - Autenticação e Operações

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Usuário" as U
participant "Auth" as A
participant "API" as API
database "Banco" as DB

U -> A : 1. Registrar/Login
A -> DB : 2. Salvar/Validar
DB --> A : 3. OK
A --> U : 4. JWT Token

U -> API : 5. Request + JWT
API -> API : 6. Validar JWT
API -> DB : 7. Operação
DB --> API : 8. Dados
API --> U : 9. Resposta

@enduml
```

## Fluxo de Terrenos e Alertas

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activityBorderColor #333
skinparam activityBackgroundColor #E1F5FF

start
:Usuário Logado;

if (Operação?) then (Criar)
  :Criar Terreno;
  :Validar Áreas;
  if (Válido?) then (Sim)
    :Salvar no Banco;
  else (Não)
    :Retornar Erro;
  endif
elseif (Listar) then (Listar)
  if (Role?) then (ROLE_USER)
    :Meus Terrenos;
  else (ROLE_ADMIN)
    :Todos os Terrenos;
  endif
elseif (Alerta) then (Gerar Alerta)
  :Chamar C# AlertEngine;
  :Gerar mensagemParaFala;
  :Enviar para Python TTS;
endif

stop

@enduml
```

## Fluxo de Recuperação de Senha

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activityBorderColor #333
skinparam activityBackgroundColor #E1F5FF

start
:Esqueceu Senha;
:Enviar CPF;
:Gerar OTP;
:Enviar SMS;
:Usuário Recebe OTP;
:Inserir OTP + Nova Senha;
:Validar OTP;

if (Válido?) then (Sim)
  :Atualizar Senha;
  :Sucesso;
else (Não)
  :Retornar Erro;
endif

stop

@enduml
```

---
## 1. Fluxo de Autenticação (Login)

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "AuthController" as Auth
participant "AuthService" as Service
database "UsuarioRepository" as Repo
participant "TokenService" as Token
database "AcessoLogRepository" as Log

Client -> Auth : POST /auth/login (cpf, senha)
Auth -> Service : login(request)
Service -> Repo : findByCpf(cpf)
Repo --> Service : Optional<Usuario>

alt CPF não encontrado
    Service --> Auth : Exception (404)
    Auth --> Client : 404 Not Found
else CPF encontrado
    Service -> Service : verificarBloqueio()
    alt Usuário bloqueado
        Service --> Auth : Exception (429)
        Auth --> Client : 429 Too Many Requests
    else Usuário não bloqueado
        Service -> Service : verificarSenha()
        alt Senha incorreta
            Service -> Service : registrarFalhaLogin()
            alt 5 tentativas falhas
                Service -> Service : bloquearUsuario(30 min)
            end
            Service -> Log : save(FAILURE)
            Service --> Auth : Exception (401)
            Auth --> Client : 401 Unauthorized
        else Senha correta
            Service -> Service : resetarTentativasFalhas()
            Service -> Token : generateToken(cpf)
            Token --> Service : JWT (8h)
            Service -> Log : save(SUCCESS)
            Service --> Auth : AuthResponse (JWT)
            Auth --> Client : 200 OK (JWT)
        end
    end
end

@enduml
```

## 2. Fluxo de Recuperação de Senha

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "AuthController" as Auth
participant "AuthService" as Service
database "UsuarioRepository" as Repo
database "PasswordRecoveryRepository" as Recovery
participant "SmsService" as SMS

Client -> Auth : POST /auth/recovery (cpf)
Auth -> Service : requestRecovery(request)
Service -> Repo : findByCpf(cpf)
Repo --> Service : Optional<Usuario>

alt CPF não encontrado
    Service --> Auth : Exception (404)
    Auth --> Client : 404 Not Found
else CPF encontrado
    Service -> Service : gerarOTP(6 dígitos)
    Service -> Recovery : save(OTP, 15 min)
    Service -> SMS : enviarSMS(cpf, OTP)
    SMS --> Service : Enviado
    Service --> Auth : 200 OK
    Auth --> Client : 200 OK (OTP enviado)
end

@enduml
```

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "AuthController" as Auth
participant "AuthService" as Service
database "PasswordRecoveryRepository" as Recovery
database "UsuarioRepository" as Repo
participant "Sha256Util" as Hash

Client -> Auth : POST /auth/reset-password (cpf, otp, novaSenha)
Auth -> Service : resetPassword(request)
Service -> Recovery : findByCpfAndOtp(cpf, otp)
Recovery --> Service : Optional<PasswordRecovery>

alt OTP não encontrado
    Service --> Auth : Exception (422)
    Auth --> Client : 422 Unprocessable Entity
else OTP encontrado
    Service -> Service : verificarExpiracao()
    alt OTP expirado
        Service --> Auth : Exception (410)
        Auth --> Client : 410 Gone
    else OTP válido
        alt 3 tentativas incorretas
            Service -> Service : bloquearFluxo()
            Service --> Auth : Exception (429)
            Auth --> Client : 429 Too Many Requests
        else Tentativas OK
            Service -> Hash : hash(novaSenha)
            Hash --> Service : senhaHash
            Service -> Repo : updateSenha(cpf, senhaHash)
            Service -> Recovery : delete(OTP)
            Service --> Auth : 200 OK
            Auth --> Client : 200 OK (Senha alterada)
        end
    end
end

@enduml
```

## 3. Fluxo de CRUD de Terrenos

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "TerrenoController" as Controller
participant "TerrenoService" as Service
database "TerrenoRepository" as Repo
database "UsuarioRepository" as UserRepo

Client -> Controller : POST /api/terrenos (terrenoRequest)
Controller -> Service : create(request)
Service -> Service : validarAreas()
alt areaReserva + areaCultivo > areaTotal
    Service --> Controller : Exception (422)
    Controller --> Client : 422 Unprocessable Entity
else Validação OK
    Service -> UserRepo : getCurrentUsuario()
    UserRepo --> Service : Usuario
    Service -> Repo : save(terreno)
    Repo --> Service : Terreno
    Service --> Controller : TerrenoResponse
    Controller --> Client : 201 Created
end

@enduml
```

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "TerrenoController" as Controller
participant "TerrenoService" as Service
database "TerrenoRepository" as Repo

Client -> Controller : GET /api/terrenos
Controller -> Service : list()
alt ROLE_USER
    Service -> Repo : findByUsuario(usuarioAtual)
    Repo --> Service : List<Terreno>
else ROLE_ADMIN
    Service -> Repo : findAll()
    Repo --> Service : List<Terreno>
end
Service --> Controller : List<TerrenoResponse>
Controller --> Client : 200 OK

@enduml
```

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "TerrenoController" as Controller
participant "TerrenoService" as Service
database "TerrenoRepository" as Repo

Client -> Controller : GET /api/terrenos/{id}
Controller -> Service : getById(id)
Service -> Repo : findById(id)
Repo --> Service : Optional<Terreno>

alt Terreno não encontrado
    Service --> Controller : Exception (404)
    Controller --> Client : 404 Not Found
else Terreno encontrado
    alt ROLE_USER e terreno de outro usuário
        Service --> Controller : Exception (404)
        Controller --> Client : 404 Not Found
    else Permissão OK
        Service --> Controller : TerrenoResponse
        Controller --> Client : 200 OK
    end
end

@enduml
```

## 4. Fluxo de Composição de Alertas

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "TerrenoAlertaController" as Controller
participant "AlertaOrquestracaoService" as Service
database "TerrenoRepository" as Repo
participant "AlertEngineClient" as Client
participant "C# AlertEngine" as CSharp

Client -> Controller : POST /api/terrenos/{id}/alertas/compor
Controller -> Service : comporAlertaParaTerreno(id, geo)
Service -> Repo : findById(id)
Repo --> Service : Optional<Terreno>

alt Terreno não encontrado
    Service --> Controller : Exception (404)
    Controller --> Client : 404 Not Found
else Terreno encontrado
    Service -> Service : montarRequest(terreno, geo)
    Service -> Client : compor(request)
    Client -> CSharp : POST /api/v1/alertas/compor
    CSharp --> Client : AlertaComposicaoResponse
    Client --> Service : AlertaComposicaoResponse
    Service --> Controller : AlertaComposicaoResponse
    Controller --> Client : 200 OK (mensagemParaFala)
end

@enduml
```

## 5. Fluxo de Segurança JWT

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

actor "Client" as Client
participant "JwtAuthenticationFilter" as Filter
participant "TokenService" as Token
participant "CustomUserDetailsService" as UserDetail
participant "SecurityContext" as Context

Client -> Filter : Request (Authorization: Bearer <token>)
Filter -> Filter : extrairToken()

alt Token não fornecido
    Filter -> Context : Authentication vazia
    Filter -> CustomAuthenticationEntryPoint : commence()
    CustomAuthenticationEntryPoint --> Client : 401 Unauthorized
else Token fornecido
    Filter -> Token : isExpired(token)
    alt Token expirado
        Filter -> Context : Authentication vazia
        Filter -> CustomAuthenticationEntryPoint : commence()
        CustomAuthenticationEntryPoint --> Client : 401 Unauthorized
    else Token válido
        Filter -> Token : extractCpf(token)
        Token --> Filter : cpf
        Filter -> UserDetail : loadUserByUsername(cpf)
        UserDetail --> Filter : UserDetails
        Filter -> Context : setAuthentication(authToken)
        Filter -> Controller : Request autorizado
        Controller --> Client : Response
    end
end

@enduml
```

## 6. Arquitetura do Sistema

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam componentStyle rectangle

package "Front-end" {
    [Cliente Web/Mobile] as Client
}

package "API Java (Spring Boot)" {
    [AuthController] as AuthController
    [UsuarioController] as UsuarioController
    [TerrenoController] as TerrenoController
    [TerrenoAlertaController] as TerrenoAlertaController
    [JwtAuthenticationFilter] as JwtFilter
    [SecurityConfig] as SecurityConfig
}

package "Services" {
    [AuthService] as AuthService
    [UsuarioService] as UsuarioService
    [TerrenoService] as TerrenoService
    [AlertaOrquestracaoService] as AlertaService
}

package "Repositories" {
    database "UsuarioRepository" as UsuarioRepo
    database "TerrenoRepository" as TerrenoRepo
    database "AcessoLogRepository" as AcessoLogRepo
    database "PasswordRecoveryRepository" as PasswordRepo
}

package "Banco de Dados" {
    database "MySQL" as MySQL
}

package "Serviço Externo C#" {
    [AgroShield.AlertEngine.Api] as AlertEngine
}

package "Serviço Python (Futuro)" {
    [Python TTS] as TTS
}

Client --> AuthController : HTTP/JWT
Client --> UsuarioController : HTTP/JWT
Client --> TerrenoController : HTTP/JWT
Client --> TerrenoAlertaController : HTTP/JWT

AuthController --> AuthService
UsuarioController --> UsuarioService
TerrenoController --> TerrenoService
TerrenoAlertaController --> AlertaService

AuthService --> UsuarioRepo
AuthService --> AcessoLogRepo
AuthService --> PasswordRepo
UsuarioService --> UsuarioRepo
TerrenoService --> TerrenoRepo

UsuarioRepo --> MySQL
TerrenoRepo --> MySQL
AcessoLogRepo --> MySQL
PasswordRepo --> MySQL

AlertaService --> AlertEngine
AlertEngine --> TTS : mensagemParaFala

JwtFilter --> SecurityConfig
SecurityConfig --> AuthController
SecurityConfig --> UsuarioController
SecurityConfig --> TerrenoController
SecurityConfig --> TerrenoAlertaController

@enduml
```

## 7. Fluxo de Limpeza de Logs (Scheduled)

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam sequenceMessageAlign center
skinparam sequenceBoxBorderColor #333
skinparam sequenceBoxBackgroundColor #FFF

participant "Scheduler" as Scheduler
participant "AuthService" as Service
database "AcessoLogRepository" as Repo
database "MySQL" as MySQL

Scheduler -> Service : limparLogsAntigos() (00:00 diário)
Service -> Service : calcularLimite(90 dias atrás)
Service -> Repo : deleteByTimestampBefore(limite)
Repo -> MySQL : DELETE FROM acesso_logs WHERE timestamp < limite
MySQL --> Repo : Registros deletados
Repo --> Service : OK
Service --> Scheduler : Concluído

@enduml
```
