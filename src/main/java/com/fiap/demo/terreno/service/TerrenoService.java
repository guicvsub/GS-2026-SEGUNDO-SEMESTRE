package com.fiap.demo.terreno.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.repository.UsuarioRepository;
import com.fiap.demo.config.SecurityUtils;
import com.fiap.demo.terreno.dto.TerrenoRequest;
import com.fiap.demo.terreno.dto.TerrenoResponse;
import com.fiap.demo.terreno.model.Terreno;
import com.fiap.demo.terreno.repository.TerrenoRepository;

@Service
public class TerrenoService {

    private final TerrenoRepository terrenoRepository;
    private final UsuarioRepository usuarioRepository;

    public TerrenoService(TerrenoRepository terrenoRepository, UsuarioRepository usuarioRepository) {
        this.terrenoRepository = terrenoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TerrenoResponse create(TerrenoRequest request) {
        Usuario usuario = getCurrentUsuario();
        validarAreas(request.areaTotalHectares(), request.areaReservaHectares(), request.areaCultivoHectares());

        Terreno terreno = Terreno.builder()
                .usuario(usuario)
                .nome(request.nome())
                .descricao(request.descricao())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .areaTotalHectares(request.areaTotalHectares())
                .areaReservaHectares(request.areaReservaHectares())
                .areaCultivoHectares(request.areaCultivoHectares())
                .emCultivo(request.emCultivo())
                .culturaAtual(request.culturaAtual())
                .tipoSolo(request.tipoSolo())
                .irrigacaoAtiva(request.irrigacaoAtiva() != null ? request.irrigacaoAtiva() : false)
                .dataReferencia(request.dataReferencia() != null ? request.dataReferencia() : LocalDate.now())
                .observacoes(request.observacoes())
                .build();

        return mapToResponse(terrenoRepository.save(terreno));
    }

    @Transactional(readOnly = true)
    public List<TerrenoResponse> list() {
        if (SecurityUtils.isAdmin()) {
            return terrenoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
        }
        String cpf = requireCurrentCpf();
        return terrenoRepository.findByUsuario_Cpf(cpf).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TerrenoResponse getById(Long id) {
        Terreno terreno = findTerrenoAutorizado(id);
        return mapToResponse(terreno);
    }

    @Transactional
    public TerrenoResponse update(Long id, TerrenoRequest request) {
        Terreno terreno = findTerrenoAutorizado(id);
        validarAreas(request.areaTotalHectares(), request.areaReservaHectares(), request.areaCultivoHectares());

        terreno.setNome(request.nome());
        terreno.setDescricao(request.descricao());
        terreno.setLatitude(request.latitude());
        terreno.setLongitude(request.longitude());
        terreno.setAreaTotalHectares(request.areaTotalHectares());
        terreno.setAreaReservaHectares(request.areaReservaHectares());
        terreno.setAreaCultivoHectares(request.areaCultivoHectares());
        terreno.setEmCultivo(request.emCultivo());
        terreno.setCulturaAtual(request.culturaAtual());
        terreno.setTipoSolo(request.tipoSolo());
        if (request.irrigacaoAtiva() != null) {
            terreno.setIrrigacaoAtiva(request.irrigacaoAtiva());
        }
        if (request.dataReferencia() != null) {
            terreno.setDataReferencia(request.dataReferencia());
        }
        terreno.setObservacoes(request.observacoes());

        return mapToResponse(terrenoRepository.save(terreno));
    }

    @Transactional
    public void delete(Long id) {
        Terreno terreno = findTerrenoAutorizado(id);
        terrenoRepository.delete(terreno);
    }

    private Terreno findTerrenoAutorizado(Long id) {
        if (SecurityUtils.isAdmin()) {
            return terrenoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terreno nao encontrado"));
        }
        String cpf = requireCurrentCpf();
        return terrenoRepository.findByIdAndUsuario_Cpf(id, cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terreno nao encontrado"));
    }

    private Usuario getCurrentUsuario() {
        String cpf = requireCurrentCpf();
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado"));
    }

    private String requireCurrentCpf() {
        String cpf = SecurityUtils.getCurrentCpf();
        if (cpf == null || cpf.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token nao fornecido");
        }
        return cpf;
    }

    private void validarAreas(BigDecimal total, BigDecimal reserva, BigDecimal cultivo) {
        BigDecimal soma = reserva.add(cultivo);
        if (soma.compareTo(total) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A soma da area reserva e area de cultivo nao pode exceder a area total");
        }
    }

    private TerrenoResponse mapToResponse(Terreno terreno) {
        return new TerrenoResponse(
                terreno.getId(),
                terreno.getUsuario().getId(),
                terreno.getNome(),
                terreno.getDescricao(),
                terreno.getLatitude(),
                terreno.getLongitude(),
                terreno.getAreaTotalHectares(),
                terreno.getAreaReservaHectares(),
                terreno.getAreaCultivoHectares(),
                terreno.getEmCultivo(),
                terreno.getCulturaAtual(),
                terreno.getTipoSolo(),
                terreno.getIrrigacaoAtiva(),
                terreno.getDataReferencia(),
                terreno.getObservacoes(),
                terreno.getCriadoEm(),
                terreno.getAtualizadoEm()
        );
    }
}
