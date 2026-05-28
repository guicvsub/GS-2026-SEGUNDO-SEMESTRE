package com.fiap.demo.terreno.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.demo.config.OpenApiConfig;
import com.fiap.demo.terreno.dto.TerrenoRequest;
import com.fiap.demo.terreno.dto.TerrenoResponse;
import com.fiap.demo.terreno.service.TerrenoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/terrenos")
@Tag(name = "Terrenos", description = "CRUD de propriedades rurais com areas em hectares e registro diario")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TerrenoController {

    private final TerrenoService terrenoService;

    public TerrenoController(TerrenoService terrenoService) {
        this.terrenoService = terrenoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "TC-TERRENO-01 — Criar terreno",
            description = "Cria terreno vinculado ao produtor autenticado. Valida soma: areaReserva + areaCultivo <= areaTotal."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Terreno criado",
                    content = @Content(schema = @Schema(implementation = TerrenoResponse.class))),
            @ApiResponse(responseCode = "401", description = "TC-TERRENO-07 — Token nao fornecido ou invalido"),
            @ApiResponse(responseCode = "422", description = "TC-TERRENO-08/10 — Areas invalidas ou dados incorretos")
    })
    public TerrenoResponse create(@RequestBody @Valid TerrenoRequest request) {
        return terrenoService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "TC-TERRENO-02 — Listar terrenos",
            description = "ROLE_USER: apenas terrenos proprios. ROLE_ADMIN (TC-TERRENO-11): todos os terrenos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TerrenoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado")
    })
    public List<TerrenoResponse> list() {
        return terrenoService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "TC-TERRENO-03 — Buscar terreno por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terreno encontrado",
                    content = @Content(schema = @Schema(implementation = TerrenoResponse.class))),
            @ApiResponse(responseCode = "404", description = "TC-TERRENO-06/09 — Nao encontrado ou sem permissao"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado")
    })
    public TerrenoResponse get(@PathVariable Long id) {
        return terrenoService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "TC-TERRENO-04 — Atualizar terreno")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terreno atualizado",
                    content = @Content(schema = @Schema(implementation = TerrenoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Terreno nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra de areas violada")
    })
    public TerrenoResponse update(@PathVariable Long id, @RequestBody @Valid TerrenoRequest request) {
        return terrenoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "TC-TERRENO-05 — Deletar terreno")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Terreno removido"),
            @ApiResponse(responseCode = "404", description = "Terreno nao encontrado")
    })
    public void delete(@PathVariable Long id) {
        terrenoService.delete(id);
    }
}
