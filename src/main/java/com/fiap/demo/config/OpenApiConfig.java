package com.fiap.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI agrosatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AGROSAT API")
                        .version("1.0")
                        .description("""
                                API do assistente agricola inteligente (Global Solution FIAP 2026).

                                ## Autenticacao
                                - Endpoints publicos: `POST /auth/register`, `POST /auth/login`, recuperacao de senha.
                                - Demais rotas `/api/**`: header `Authorization: Bearer <JWT>` (expira em 8h).
                                - Senhas armazenadas com **BCrypt** no campo `senha_hash`.

                                ## Papeis
                                - `ROLE_USER`: produtor — gerencia apenas seus terrenos.
                                - `ROLE_ADMIN`: acesso total e estatisticas agregadas.

                                ## Cenarios de teste — Terrenos (TDD)
                                | ID | Cenario | HTTP |
                                |----|---------|------|
                                | TC-TERRENO-01 | Criar terreno valido | 201 |
                                | TC-TERRENO-02 | Listar terrenos do usuario | 200 |
                                | TC-TERRENO-03 | Buscar por id | 200 |
                                | TC-TERRENO-04 | Atualizar terreno | 200 |
                                | TC-TERRENO-05 | Deletar terreno | 204 |
                                | TC-TERRENO-06 | Buscar apos delete | 404 |
                                | TC-TERRENO-07 | Sem token | 401 |
                                | TC-TERRENO-08 | Area reserva + cultivo > total | 422 |
                                | TC-TERRENO-09 | Acesso a terreno de outro produtor | 404 |
                                | TC-TERRENO-10 | Payload invalido | 422 |
                                | TC-TERRENO-11 | Admin lista todos os terrenos | 200 |
                                """)
                        .contact(new Contact().name("AGROSAT FIAP").email("contato@agrosat.local")))
                .addTagsItem(new Tag().name("Autenticacao").description("Cadastro, login e recuperacao de senha (RF-AUTH)"))
                .addTagsItem(new Tag().name("Usuarios").description("CRUD de usuarios — restrito por papel"))
                .addTagsItem(new Tag().name("Terrenos").description("CRUD de terrenos em hectares — TC-TERRENO-01 a 11"))
                .addTagsItem(new Tag().name("Lavoura").description("Rota protegida de exemplo para validacao JWT"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtido em POST /auth/login ou POST /auth/register")));
    }
}
