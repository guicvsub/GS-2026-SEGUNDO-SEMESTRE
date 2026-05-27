package com.fiap.demo.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;

import com.fiap.demo.auth.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("AUTH - Suite de testes de autenticacao (Agro-GS)")
class AuthContractTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void cleanup() {
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Cadastro (RF-AUTH-01)")
    class RegisterContract {

        @Test
        @DisplayName("TC-AUTH-01 - Cadastro de usuario valido -> HTTP 201 + JWT + SMS")
        void tcAuth01ShouldRegisterValidUser() throws Exception {
            HttpResponse<String> response = post("/auth/register", """
                    {
                      "cpf":"529.982.247-25",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """, null);

            assertEquals(HttpStatus.CREATED.value(), response.statusCode());
            assertContains(response.body(), "\"token\":");
            assertContains(response.body(), "\"expiresInHours\":8");
            assertContains(response.body(), "\"smsSent\":true");
        }

        @Test
        @DisplayName("TC-AUTH-02 - Cadastro com CPF duplicado -> HTTP 409 + 'CPF ja cadastrado'")
        void tcAuth02ShouldRejectDuplicatedCpf() throws Exception {
            String payload = """
                    {
                      "cpf":"153.509.460-56",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """;
            post("/auth/register", payload, null);
            HttpResponse<String> response = post("/auth/register", payload, null);

            assertEquals(HttpStatus.CONFLICT.value(), response.statusCode());
            assertContains(response.body(), "CPF ja cadastrado");
        }

        @Test
        @DisplayName("TC-AUTH-03 - Cadastro com CPF invalido -> HTTP 422")
        void tcAuth03ShouldRejectInvalidCpf() throws Exception {
            HttpResponse<String> response = post("/auth/register", """
                    {
                      "cpf":"000.000.000-00",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """, null);

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode());
        }
    }

    @Nested
    @DisplayName("Login (RF-AUTH-02)")
    class LoginContract {

        @Test
        @DisplayName("TC-AUTH-04 - Login com credenciais corretas -> HTTP 200 + JWT exp 8h")
        void tcAuth04ShouldLoginWithValidCredentials() throws Exception {
            post("/auth/register", """
                    {
                      "cpf":"111.444.777-35",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """, null);

            HttpResponse<String> response = post("/auth/login", """
                    {
                      "cpf":"111.444.777-35",
                      "senha":"Farm@2026"
                    }
                    """, null);

            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertContains(response.body(), "\"token\":");
            assertContains(response.body(), "\"expiresInHours\":8");
        }

        @Test
        @DisplayName("TC-AUTH-05 - Login com senha incorreta -> HTTP 401 + tentativas decrementadas")
        void tcAuth05ShouldRejectWrongPassword() throws Exception {
            post("/auth/register", """
                    {
                      "cpf":"390.533.447-05",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """, null);

            HttpResponse<String> response = post("/auth/login", """
                    {
                      "cpf":"390.533.447-05",
                      "senha":"errada123"
                    }
                    """, null);

            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
        }

        @Test
        @DisplayName("TC-AUTH-06 - Bloqueio apos 5 tentativas -> HTTP 429 + bloqueio 30 min + SMS")
        void tcAuth06ShouldBlockAfterFiveAttempts() throws Exception {
            post("/auth/register", """
                    {
                      "cpf":"529.982.247-25",
                      "nome":"Joao",
                      "senha":"Farm@2026",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaCultivo":12.5
                    }
                    """, null);

            for (int i = 0; i < 5; i++) {
                HttpResponse<String> unauthorized = post("/auth/login", """
                        {
                          "cpf":"529.982.247-25",
                          "senha":"errada123"
                        }
                        """, null);
                assertEquals(HttpStatus.UNAUTHORIZED.value(), unauthorized.statusCode());
            }

            HttpResponse<String> blocked = post("/auth/login", """
                    {
                      "cpf":"529.982.247-25",
                      "senha":"errada123"
                    }
                    """, null);
            assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), blocked.statusCode());
        }
    }

    @Nested
    @DisplayName("Seguranca de rotas protegidas (JWT)")
    class ProtectedRouteContract {

        @Test
        @DisplayName("TC-AUTH-07 - Acesso a rota protegida sem token -> HTTP 401 + 'Token nao fornecido'")
        void tcAuth07ShouldDenyRequestWithoutToken() throws Exception {
            HttpResponse<String> response = get("/api/lavoura", null);
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
            assertContains(response.body(), "Token nao fornecido");
        }

        @Test
        @DisplayName("TC-AUTH-08 - Acesso com token expirado -> HTTP 401 + 'Token expirado'")
        void tcAuth08ShouldDenyRequestWithExpiredToken() throws Exception {
            HttpResponse<String> response = get("/api/lavoura", "Bearer expired.jwt.token");
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
            assertContains(response.body(), "Token expirado");
        }
    }

    private HttpResponse<String> post(String path, String jsonBody, String bearerToken) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            requestBuilder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String bearerToken) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET();
        if (bearerToken != null) {
            requestBuilder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertContains(String responseBody, String expected) {
        assertNotNull(responseBody);
        if (!responseBody.contains(expected)) {
            throw new AssertionError("Resposta nao contem esperado: " + expected + " body=" + responseBody);
        }
    }
}
