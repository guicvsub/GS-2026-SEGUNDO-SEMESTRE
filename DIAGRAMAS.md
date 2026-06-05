# Diagramas de Fluxo - AgroShield

## Diagrama Simplificado - Visão Geral

```mermaid
graph LR
    A[Cliente] -->|1. Registro/Login| B[AuthController]
    A -->|2. JWT Token| C[API Protegida]
    B -->|3. JWT 8h| C
    C -->|4. CRUD Terrenos| D[TerrenoController]
    C -->|5. CRUD Usuarios| E[UsuarioController]
    D -->|6. Alertas| F[AlertEngine C#]
    F -->|7. mensagemParaFala| G[Python TTS]
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#e8f5e9
    style D fill:#fff4e1
    style E fill:#fff4e1
    style F fill:#f3e5f5
    style G fill:#fce4ec
```

## Fluxo Principal - Autenticação e Operações

```mermaid
sequenceDiagram
    participant U as Usuário
    participant A as Auth
    participant API as API
    participant DB as Banco

    U->>A: 1. Registrar/Login
    A->>DB: 2. Salvar/Validar
    DB-->>A: 3. OK
    A-->>U: 4. JWT Token
    
    U->>API: 5. Request + JWT
    API->>API: 6. Validar JWT
    API->>DB: 7. Operação
    DB-->>API: 8. Dados
    API-->>U: 9. Resposta
```

## Fluxo de Terrenos e Alertas

```mermaid
graph TB
    Start[Usuário Logado] --> Terreno[CRUD Terrenos]
    Terreno --> Criar[Criar Terreno]
    Terreno --> Listar[Listar Terrenos]
    Terreno --> Alerta[Gerar Alerta]
    
    Criar --> Valida[Validar Áreas]
    Valida -->|OK| Salvar[Salvar no Banco]
    Valida -->|Erro| Erro[Retornar Erro]
    
    Listar --> User[ROLE_USER: Meus Terrenos]
    Listar --> Admin[ROLE_ADMIN: Todos]
    
    Alerta --> CSharp[Chamar C# AlertEngine]
    CSharp --> Mensagem[Gerar mensagemParaFala]
    Mensagem --> TTS[Enviar para Python TTS]
    
    style Start fill:#e1f5ff
    style Terreno fill:#fff4e1
    style Alerta fill:#f3e5f5
    style CSharp fill:#f3e5f5
    style TTS fill:#fce4ec
```

## Fluxo de Recuperação de Senha

```mermaid
graph LR
    A[Esqueceu Senha] --> B[Enviar CPF]
    B --> C[Gerar OTP]
    C --> D[Enviar SMS]
    D --> E[Usuário Recebe OTP]
    E --> F[Inserir OTP + Nova Senha]
    F --> G[Validar OTP]
    G -->|OK| H[Atualizar Senha]
    G -->|Erro| I[Retornar Erro]
    H --> J[Sucesso]
    
    style A fill:#e1f5ff
    style C fill:#fff4e1
    style G fill:#fff4e1
    style H fill:#e8f5e9
    style J fill:#e8f5e9
```

---
## 1. Fluxo de Autenticação (Login)

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant UsuarioRepository
    participant TokenService
    participant AcessoLogRepository

    Client->>AuthController: POST /auth/login (cpf, senha)
    AuthController->>AuthService: login(request)
    AuthService->>UsuarioRepository: findByCpf(cpf)
    UsuarioRepository-->>AuthService: Optional<Usuario>
    
    alt CPF não encontrado
        AuthService-->>AuthController: Exception (404)
        AuthController-->>Client: 404 Not Found
    else CPF encontrado
        AuthService->>AuthService: verificarBloqueio()
        alt Usuário bloqueado
            AuthService-->>AuthController: Exception (429)
            AuthController-->>Client: 429 Too Many Requests
        else Usuário não bloqueado
            AuthService->>AuthService: verificarSenha()
            alt Senha incorreta
                AuthService->>AuthService: registrarFalhaLogin()
                alt 5 tentativas falhas
                    AuthService->>AuthService: bloquearUsuario(30 min)
                end
                AuthService->>AcessoLogRepository: save(FAILURE)
                AuthService-->>AuthController: Exception (401)
                AuthController-->>Client: 401 Unauthorized
            else Senha correta
                AuthService->>AuthService: resetarTentativasFalhas()
                AuthService->>TokenService: generateToken(cpf)
                TokenService-->>AuthService: JWT (8h)
                AuthService->>AcessoLogRepository: save(SUCCESS)
                AuthService-->>AuthController: AuthResponse (JWT)
                AuthController-->>Client: 200 OK (JWT)
            end
        end
    end
```

## 2. Fluxo de Recuperação de Senha

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant UsuarioRepository
    participant PasswordRecoveryRepository
    participant SmsService

    Client->>AuthController: POST /auth/recovery (cpf)
    AuthController->>AuthService: requestRecovery(request)
    AuthService->>UsuarioRepository: findByCpf(cpf)
    UsuarioRepository-->>AuthService: Optional<Usuario>
    
    alt CPF não encontrado
        AuthService-->>AuthController: Exception (404)
        AuthController-->>Client: 404 Not Found
    else CPF encontrado
        AuthService->>AuthService: gerarOTP(6 dígitos)
        AuthService->>PasswordRecoveryRepository: save(OTP, 15 min)
        AuthService->>SmsService: enviarSMS(cpf, OTP)
        SmsService-->>AuthService: Enviado
        AuthService-->>AuthController: 200 OK
        AuthController-->>Client: 200 OK (OTP enviado)
    end
```

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant PasswordRecoveryRepository
    participant UsuarioRepository
    participant Sha256Util

    Client->>AuthController: POST /auth/reset-password (cpf, otp, novaSenha)
    AuthController->>AuthService: resetPassword(request)
    AuthService->>PasswordRecoveryRepository: findByCpfAndOtp(cpf, otp)
    PasswordRecoveryRepository-->>AuthService: Optional<PasswordRecovery>
    
    alt OTP não encontrado
        AuthService-->>AuthController: Exception (422)
        AuthController-->>Client: 422 Unprocessable Entity
    else OTP encontrado
        AuthService->>AuthService: verificarExpiracao()
        alt OTP expirado
            AuthService-->>AuthController: Exception (410)
            AuthController-->>Client: 410 Gone
        else OTP válido
            alt 3 tentativas incorretas
                AuthService->>AuthService: bloquearFluxo()
                AuthService-->>AuthController: Exception (429)
                AuthController-->>Client: 429 Too Many Requests
            else Tentativas OK
                AuthService->>Sha256Util: hash(novaSenha)
                Sha256Util-->>AuthService: senhaHash
                AuthService->>UsuarioRepository: updateSenha(cpf, senhaHash)
                AuthService->>PasswordRecoveryRepository: delete(OTP)
                AuthService-->>AuthController: 200 OK
                AuthController-->>Client: 200 OK (Senha alterada)
            end
        end
    end
```

## 3. Fluxo de CRUD de Terrenos

```mermaid
sequenceDiagram
    participant Client
    participant TerrenoController
    participant TerrenoService
    participant TerrenoRepository
    participant UsuarioRepository

    Client->>TerrenoController: POST /api/terrenos (terrenoRequest)
    TerrenoController->>TerrenoService: create(request)
    TerrenoService->>TerrenoService: validarAreas()
    alt areaReserva + areaCultivo > areaTotal
        TerrenoService-->>TerrenoController: Exception (422)
        TerrenoController-->>Client: 422 Unprocessable Entity
    else Validação OK
        TerrenoService->>UsuarioRepository: getCurrentUsuario()
        UsuarioRepository-->>TerrenoService: Usuario
        TerrenoService->>TerrenoRepository: save(terreno)
        TerrenoRepository-->>TerrenoService: Terreno
        TerrenoService-->>TerrenoController: TerrenoResponse
        TerrenoController-->>Client: 201 Created
    end
```

```mermaid
sequenceDiagram
    participant Client
    participant TerrenoController
    participant TerrenoService
    participant TerrenoRepository

    Client->>TerrenoController: GET /api/terrenos
    TerrenoController->>TerrenoService: list()
    alt ROLE_USER
        TerrenoService->>TerrenoRepository: findByUsuario(usuarioAtual)
        TerrenoRepository-->>TerrenoService: List<Terreno>
    else ROLE_ADMIN
        TerrenoService->>TerrenoRepository: findAll()
        TerrenoRepository-->>TerrenoService: List<Terreno>
    end
    TerrenoService-->>TerrenoController: List<TerrenoResponse>
    TerrenoController-->>Client: 200 OK
```

```mermaid
sequenceDiagram
    participant Client
    participant TerrenoController
    participant TerrenoService
    participant TerrenoRepository

    Client->>TerrenoController: GET /api/terrenos/{id}
    TerrenoController->>TerrenoService: getById(id)
    TerrenoService->>TerrenoRepository: findById(id)
    TerrenoRepository-->>TerrenoService: Optional<Terreno>
    
    alt Terreno não encontrado
        TerrenoService-->>TerrenoController: Exception (404)
        TerrenoController-->>Client: 404 Not Found
    else Terreno encontrado
        alt ROLE_USER e terreno de outro usuário
            TerrenoService-->>TerrenoController: Exception (404)
            TerrenoController-->>Client: 404 Not Found
        else Permissão OK
            TerrenoService-->>TerrenoController: TerrenoResponse
            TerrenoController-->>Client: 200 OK
        end
    end
```

## 4. Fluxo de Composição de Alertas

```mermaid
sequenceDiagram
    participant Client
    participant TerrenoAlertaController
    participant AlertaOrquestracaoService
    participant TerrenoRepository
    participant AlertEngineClient
    participant C# AlertEngine

    Client->>TerrenoAlertaController: POST /api/terrenos/{id}/alertas/compor
    TerrenoAlertaController->>AlertaOrquestracaoService: comporAlertaParaTerreno(id, geo)
    AlertaOrquestracaoService->>TerrenoRepository: findById(id)
    TerrenoRepository-->>AlertaOrquestracaoService: Optional<Terreno>
    
    alt Terreno não encontrado
        AlertaOrquestracaoService-->>TerrenoAlertaController: Exception (404)
        TerrenoAlertaController-->>Client: 404 Not Found
    else Terreno encontrado
        AlertaOrquestracaoService->>AlertaOrquestracaoService: montarRequest(terreno, geo)
        AlertaOrquestracaoService->>AlertEngineClient: compor(request)
        AlertEngineClient->>C# AlertEngine: POST /api/v1/alertas/compor
        C# AlertEngine-->>AlertEngineClient: AlertaComposicaoResponse
        AlertEngineClient-->>AlertaOrquestracaoService: AlertaComposicaoResponse
        AlertaOrquestracaoService-->>TerrenoAlertaController: AlertaComposicaoResponse
        TerrenoAlertaController-->>Client: 200 OK (mensagemParaFala)
    end
```

## 5. Fluxo de Segurança JWT

```mermaid
sequenceDiagram
    participant Client
    participant JwtAuthenticationFilter
    participant TokenService
    participant CustomUserDetailsService
    participant SecurityContext

    Client->>JwtAuthenticationFilter: Request (Authorization: Bearer <token>)
    JwtAuthenticationFilter->>JwtAuthenticationFilter: extrairToken()
    
    alt Token não fornecido
        JwtAuthenticationFilter->>SecurityContext: Authentication vazia
        JwtAuthenticationFilter->>CustomAuthenticationEntryPoint: commence()
        CustomAuthenticationEntryPoint-->>Client: 401 Unauthorized
    else Token fornecido
        JwtAuthenticationFilter->>TokenService: isExpired(token)
        alt Token expirado
            JwtAuthenticationFilter->>SecurityContext: Authentication vazia
            JwtAuthenticationFilter->>CustomAuthenticationEntryPoint: commence()
            CustomAuthenticationEntryPoint-->>Client: 401 Unauthorized
        else Token válido
            JwtAuthenticationFilter->>TokenService: extractCpf(token)
            TokenService-->>JwtAuthenticationFilter: cpf
            JwtAuthenticationFilter->>CustomUserDetailsService: loadUserByUsername(cpf)
            CustomUserDetailsService-->>JwtAuthenticationFilter: UserDetails
            JwtAuthenticationFilter->>SecurityContext: setAuthentication(authToken)
            JwtAuthenticationFilter->>Controller: Request autorizado
            Controller-->>Client: Response
        end
    end
```

## 6. Arquitetura do Sistema

```mermaid
graph TB
    subgraph "Front-end"
        Client[Cliente Web/Mobile]
    end
    
    subgraph "API Java (Spring Boot)"
        AuthController[AuthController]
        UsuarioController[UsuarioController]
        TerrenoController[TerrenoController]
        TerrenoAlertaController[TerrenoAlertaController]
        JwtFilter[JwtAuthenticationFilter]
        SecurityConfig[SecurityConfig]
    end
    
    subgraph "Services"
        AuthService[AuthService]
        UsuarioService[UsuarioService]
        TerrenoService[TerrenoService]
        AlertaOrquestracaoService[AlertaOrquestracaoService]
    end
    
    subgraph "Repositories"
        UsuarioRepository[UsuarioRepository]
        TerrenoRepository[TerrenoRepository]
        AcessoLogRepository[AcessoLogRepository]
        PasswordRecoveryRepository[PasswordRecoveryRepository]
    end
    
    subgraph "Banco de Dados"
        MySQL[(MySQL)]
    end
    
    subgraph "Serviço Externo C#"
        AlertEngine[AgroShield.AlertEngine.Api]
    end
    
    subgraph "Serviço Python (Futuro)"
        TTS[Python TTS]
    end
    
    Client -->|HTTP/JWT| AuthController
    Client -->|HTTP/JWT| UsuarioController
    Client -->|HTTP/JWT| TerrenoController
    Client -->|HTTP/JWT| TerrenoAlertaController
    
    AuthController --> AuthService
    UsuarioController --> UsuarioService
    TerrenoController --> TerrenoService
    TerrenoAlertaController --> AlertaOrquestracaoService
    
    AuthService --> UsuarioRepository
    AuthService --> AcessoLogRepository
    AuthService --> PasswordRecoveryRepository
    UsuarioService --> UsuarioRepository
    TerrenoService --> TerrenoRepository
    
    UsuarioRepository --> MySQL
    TerrenoRepository --> MySQL
    AcessoLogRepository --> MySQL
    PasswordRecoveryRepository --> MySQL
    
    AlertaOrquestracaoService --> AlertEngine
    AlertEngine -->|mensagemParaFala| TTS
    
    JwtFilter --> SecurityConfig
    SecurityConfig --> AuthController
    SecurityConfig --> UsuarioController
    SecurityConfig --> TerrenoController
    SecurityConfig --> TerrenoAlertaController
```

## 7. Fluxo de Limpeza de Logs (Scheduled)

```mermaid
sequenceDiagram
    participant Scheduler
    participant AuthService
    participant AcessoLogRepository
    participant MySQL

    Scheduler->>AuthService: limparLogsAntigos() (00:00 diário)
    AuthService->>AuthService: calcularLimite(90 dias atrás)
    AuthService->>AcessoLogRepository: deleteByTimestampBefore(limite)
    AcessoLogRepository->>MySQL: DELETE FROM acesso_logs WHERE timestamp < limite
    MySQL-->>AcessoLogRepository: Registros deletados
    AcessoLogRepository-->>AuthService: OK
    AuthService-->>Scheduler: Concluído
```
