package com.fiap.demo.terreno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import com.fiap.demo.auth.model.Role;
import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.repository.UsuarioRepository;
import com.fiap.demo.terreno.repository.TerrenoRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("TERRENO - Suite de testes CRUD (Agro-GS)")
class TerrenoContractTest {

    private static final String SENHA = "Farm@2026";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TerrenoRepository terrenoRepository;

    @BeforeEach
    void cleanup() {
        terrenoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("CRUD completo (RF-TERRENO)")
    class CrudContract {

        @Test
        @DisplayName("TC-TERRENO-01 - Criar terreno valido -> HTTP 201 + campos em hectares")
        void tcTerreno01ShouldCreateValidTerreno() throws Exception {
            String token = registerAndToken("111.444.777-35");

            HttpResponse<String> response = post("/api/terrenos", terrenoPayload("Lavoura Norte"), bearer(token));

            assertEquals(HttpStatus.CREATED.value(), response.statusCode());
            assertContains(response.body(), "\"nome\":\"Lavoura Norte\"");
            assertContains(response.body(), "\"areaTotalHectares\":50");
            assertContains(response.body(), "\"areaReservaHectares\":10");
            assertContains(response.body(), "\"areaCultivoHectares\":35");
            assertContains(response.body(), "\"emCultivo\":true");
            assertContains(response.body(), "\"culturaAtual\":\"Soja\"");
        }

        @Test
        @DisplayName("TC-TERRENO-02 - Listar terrenos do usuario -> HTTP 200")
        void tcTerreno02ShouldListOwnTerrenos() throws Exception {
            String token = registerAndToken("529.982.247-25");
            post("/api/terrenos", terrenoPayload("Talhao A"), bearer(token));
            post("/api/terrenos", terrenoPayload("Talhao B"), bearer(token));

            HttpResponse<String> response = get("/api/terrenos", bearer(token));

            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertContains(response.body(), "Talhao A");
            assertContains(response.body(), "Talhao B");
        }

        @Test
        @DisplayName("TC-TERRENO-03 - Buscar terreno por id -> HTTP 200")
        void tcTerreno03ShouldGetTerrenoById() throws Exception {
            String token = registerAndToken("390.533.447-05");
            HttpResponse<String> created = post("/api/terrenos", terrenoPayload("Lavoura Sul"), bearer(token));
            long id = extractId(created.body());

            HttpResponse<String> response = get("/api/terrenos/" + id, bearer(token));

            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertContains(response.body(), "\"id\":" + id);
            assertContains(response.body(), "\"nome\":\"Lavoura Sul\"");
        }

        @Test
        @DisplayName("TC-TERRENO-04 - Atualizar terreno -> HTTP 200")
        void tcTerreno04ShouldUpdateTerreno() throws Exception {
            String token = registerAndToken("153.509.460-56");
            HttpResponse<String> created = post("/api/terrenos", terrenoPayload("Antes"), bearer(token));
            long id = extractId(created.body());

            HttpResponse<String> response = put("/api/terrenos/" + id, """
                    {
                      "nome":"Depois",
                      "descricao":"Atualizado",
                      "latitude":-15.71,
                      "longitude":-47.91,
                      "areaTotalHectares":50.00,
                      "areaReservaHectares":10.00,
                      "areaCultivoHectares":38.00,
                      "emCultivo":true,
                      "culturaAtual":"Milho",
                      "tipoSolo":"Argiloso",
                      "irrigacaoAtiva":false,
                      "dataReferencia":"2026-05-28",
                      "observacoes":"Rotacao de cultura"
                    }
                    """, bearer(token));

            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertContains(response.body(), "\"nome\":\"Depois\"");
            assertContains(response.body(), "\"culturaAtual\":\"Milho\"");
            assertContains(response.body(), "\"areaCultivoHectares\":38");
        }

        @Test
        @DisplayName("TC-TERRENO-05 - Deletar terreno -> HTTP 204")
        void tcTerreno05ShouldDeleteTerreno() throws Exception {
            String token = registerAndToken("111.444.777-35");
            HttpResponse<String> created = post("/api/terrenos", terrenoPayload("Para Excluir"), bearer(token));
            long id = extractId(created.body());

            HttpResponse<String> response = delete("/api/terrenos/" + id, bearer(token));

            assertEquals(HttpStatus.NO_CONTENT.value(), response.statusCode());
        }

        @Test
        @DisplayName("TC-TERRENO-06 - Buscar terreno deletado -> HTTP 404")
        void tcTerreno06ShouldReturn404AfterDelete() throws Exception {
            String token = registerAndToken("529.982.247-25");
            HttpResponse<String> created = post("/api/terrenos", terrenoPayload("Temp"), bearer(token));
            long id = extractId(created.body());
            delete("/api/terrenos/" + id, bearer(token));

            HttpResponse<String> response = get("/api/terrenos/" + id, bearer(token));

            assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
            assertContains(response.body(), "Terreno nao encontrado");
        }
    }

    @Nested
    @DisplayName("Regras de negocio e seguranca")
    class BusinessAndSecurityContract {

        @Test
        @DisplayName("TC-TERRENO-07 - Criar terreno sem token -> HTTP 401")
        void tcTerreno07ShouldRejectWithoutToken() throws Exception {
            HttpResponse<String> response = post("/api/terrenos", terrenoPayload("Sem Auth"), null);

            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
            assertContains(response.body(), "Token nao fornecido");
        }

        @Test
        @DisplayName("TC-TERRENO-08 - Area reserva + cultivo maior que total -> HTTP 422")
        void tcTerreno08ShouldRejectInvalidAreaSum() throws Exception {
            String token = registerAndToken("390.533.447-05");

            HttpResponse<String> response = post("/api/terrenos", """
                    {
                      "nome":"Area Invalida",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaTotalHectares":20.00,
                      "areaReservaHectares":15.00,
                      "areaCultivoHectares":10.00,
                      "emCultivo":true
                    }
                    """, bearer(token));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode());
            assertContains(response.body(), "nao pode exceder a area total");
        }

        @Test
        @DisplayName("TC-TERRENO-09 - Usuario nao acessa terreno de outro produtor -> HTTP 404")
        void tcTerreno09ShouldDenyAccessToOtherUserTerreno() throws Exception {
            String tokenA = registerAndToken("111.444.777-35");
            String tokenB = registerAndToken("529.982.247-25");

            HttpResponse<String> created = post("/api/terrenos", terrenoPayload("Terreno do A"), bearer(tokenA));
            long id = extractId(created.body());

            HttpResponse<String> response = get("/api/terrenos/" + id, bearer(tokenB));

            assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
        }

        @Test
        @DisplayName("TC-TERRENO-10 - Payload invalido (nome vazio) -> HTTP 422")
        void tcTerreno10ShouldRejectInvalidPayload() throws Exception {
            String token = registerAndToken("153.509.460-56");

            HttpResponse<String> response = post("/api/terrenos", """
                    {
                      "nome":"",
                      "latitude":-15.7,
                      "longitude":-47.9,
                      "areaTotalHectares":50.00,
                      "areaReservaHectares":10.00,
                      "areaCultivoHectares":35.00,
                      "emCultivo":true
                    }
                    """, bearer(token));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode());
        }

        @Test
        @DisplayName("TC-TERRENO-11 - Admin lista terrenos de todos os usuarios -> HTTP 200")
        void tcTerreno11AdminShouldListAllTerrenos() throws Exception {
            String tokenUser = registerAndToken("111.444.777-35");
            registerAndToken("529.982.247-25");

            post("/api/terrenos", terrenoPayload("Do User 1"), bearer(tokenUser));

            Usuario admin = usuarioRepository.findByCpf("52998224725").orElseThrow();
            admin.setRole(Role.ROLE_ADMIN);
            usuarioRepository.save(admin);

            String tokenAdmin = loginAndToken("529.982.247-25");
            HttpResponse<String> response = get("/api/terrenos", bearer(tokenAdmin));

            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertContains(response.body(), "Do User 1");
        }
    }

    private String registerAndToken(String cpf) throws IOException, InterruptedException {
        post("/auth/register", registerPayload(cpf), null);
        return loginAndToken(cpf);
    }

    private String loginAndToken(String cpf) throws IOException, InterruptedException {
        HttpResponse<String> response = post("/auth/login", loginPayload(cpf), null);
        assertEquals(HttpStatus.OK.value(), response.statusCode());
        return extractToken(response.body());
    }

    private String registerPayload(String cpf) {
        return """
                {
                  "cpf":"%s",
                  "nome":"Produtor Teste",
                  "senha":"%s",
                  "latitude":-15.7,
                  "longitude":-47.9,
                  "areaCultivo":12.5
                }
                """.formatted(cpf, SENHA);
    }

    private String loginPayload(String cpf) {
        return """
                {
                  "cpf":"%s",
                  "senha":"%s"
                }
                """.formatted(cpf, SENHA);
    }

    private String terrenoPayload(String nome) {
        return """
                {
                  "nome":"%s",
                  "descricao":"Talhao de teste",
                  "latitude":-15.7000000,
                  "longitude":-47.9000000,
                  "areaTotalHectares":50.00,
                  "areaReservaHectares":10.00,
                  "areaCultivoHectares":35.00,
                  "emCultivo":true,
                  "culturaAtual":"Soja",
                  "tipoSolo":"Argiloso",
                  "irrigacaoAtiva":true,
                  "dataReferencia":"2026-05-28",
                  "observacoes":"Registro diario"
                }
                """.formatted(nome);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extractToken(String body) {
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        assertTrue(matcher.find(), "Token JWT nao encontrado na resposta: " + body);
        return matcher.group(1);
    }

    private long extractId(String body) {
        Matcher matcher = ID_PATTERN.matcher(body);
        assertTrue(matcher.find(), "ID nao encontrado na resposta: " + body);
        return Long.parseLong(matcher.group(1));
    }

    private HttpResponse<String> post(String path, String jsonBody, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            builder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String bearerToken) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET();
        if (bearerToken != null) {
            builder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String jsonBody, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            builder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String bearerToken) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .DELETE();
        if (bearerToken != null) {
            builder.header("Authorization", bearerToken);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertContains(String responseBody, String expected) {
        assertNotNull(responseBody);
        if (!responseBody.contains(expected)) {
            throw new AssertionError("Resposta nao contem esperado: " + expected + " body=" + responseBody);
        }
    }
}
