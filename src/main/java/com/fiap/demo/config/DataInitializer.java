package com.fiap.demo.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.demo.auth.model.Role;
import com.fiap.demo.auth.model.Usuario;
import com.fiap.demo.auth.repository.UsuarioRepository;
import com.fiap.demo.auth.util.Sha256Util;
import com.fiap.demo.terreno.model.Terreno;
import com.fiap.demo.terreno.repository.TerrenoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TerrenoRepository terrenoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository, TerrenoRepository terrenoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.terrenoRepository = terrenoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando populacao de dados de teste...");
        
        // Limpar dados existentes (opcional - remover se quiser manter dados)
        // terrenoRepository.deleteAll();
        // usuarioRepository.deleteAll();
        
        // Criar usuários de teste
        if (usuarioRepository.count() == 0) {
            criarUsuarios();
        } else {
            log.info("Usuarios ja existem no banco, pulando criacao.");
        }
        
        // Criar terrenos de teste
        if (terrenoRepository.count() == 0) {
            criarTerrenos();
        } else {
            log.info("Terrenos ja existem no banco, pulando criacao.");
        }
        
        log.info("Populacao de dados de teste concluida.");
    }
    
    private void criarUsuarios() {
        log.info("Criando usuarios de teste...");
        
        // Usuário comum (ROLE_USER)
        Usuario usuarioComum = Usuario.builder()
                .nome("Joao Silva")
                .cpf("529.982.247-25")
                .senhaHash(Sha256Util.hash("Farm@2026"))
                .latitude(new BigDecimal("-15.7"))
                .longitude(new BigDecimal("-47.9"))
                .areaCultivoHectares(new BigDecimal("50.0"))
                .role(Role.ROLE_USER)
                .build();
        usuarioRepository.save(usuarioComum);
        log.info("Usuario comum criado: CPF={}, Nome={}", usuarioComum.getCpf(), usuarioComum.getNome());
        
        // Usuário admin (ROLE_ADMIN)
        Usuario usuarioAdmin = Usuario.builder()
                .nome("Admin Sistema")
                .cpf("123.456.789-09")
                .senhaHash(Sha256Util.hash("Admin@2026"))
                .latitude(new BigDecimal("-15.8"))
                .longitude(new BigDecimal("-47.8"))
                .areaCultivoHectares(new BigDecimal("100.0"))
                .role(Role.ROLE_ADMIN)
                .build();
        usuarioRepository.save(usuarioAdmin);
        log.info("Usuario admin criado: CPF={}, Nome={}", usuarioAdmin.getCpf(), usuarioAdmin.getNome());
        
        // Usuário comum adicional
        Usuario usuarioComum2 = Usuario.builder()
                .nome("Maria Santos")
                .cpf("987.654.321-00")
                .senhaHash(Sha256Util.hash("Maria@2026"))
                .latitude(new BigDecimal("-16.0"))
                .longitude(new BigDecimal("-48.0"))
                .areaCultivoHectares(new BigDecimal("75.0"))
                .role(Role.ROLE_USER)
                .build();
        usuarioRepository.save(usuarioComum2);
        log.info("Usuario comum adicional criado: CPF={}, Nome={}", usuarioComum2.getCpf(), usuarioComum2.getNome());
    }
    
    private void criarTerrenos() {
        log.info("Criando terrenos de teste...");
        
        // Buscar usuários para associar aos terrenos
        Usuario usuarioComum = usuarioRepository.findByCpf("529.982.247-25")
                .orElseThrow(() -> new RuntimeException("Usuario comum nao encontrado"));
        Usuario usuarioAdmin = usuarioRepository.findByCpf("123.456.789-09")
                .orElseThrow(() -> new RuntimeException("Usuario admin nao encontrado"));
        
        // Terreno do usuário comum
        Terreno terreno1 = Terreno.builder()
                .nome("Fazenda Santa Luzia")
                .descricao("Propriedade rural dedicada ao cultivo de soja e milho")
                .latitude(new BigDecimal("-15.7"))
                .longitude(new BigDecimal("-47.9"))
                .areaTotalHectares(new BigDecimal("100.0"))
                .areaReservaHectares(new BigDecimal("20.0"))
                .areaCultivoHectares(new BigDecimal("80.0"))
                .emCultivo(true)
                .culturaAtual("Soja")
                .tipoSolo("Argiloso")
                .irrigacaoAtiva(true)
                .dataReferencia(LocalDate.of(2026, 6, 1))
                .observacoes("Solo bem drenado, ideal para cultivo de grãos")
                .usuario(usuarioComum)
                .build();
        terrenoRepository.save(terreno1);
        log.info("Terreno criado: ID={}, Nome={}, Usuario={}", terreno1.getId(), terreno1.getNome(), usuarioComum.getCpf());
        
        // Terreno do usuário admin
        Terreno terreno2 = Terreno.builder()
                .nome("Fazenda Boa Esperanca")
                .descricao("Propriedade rural com cultivo diversificado")
                .latitude(new BigDecimal("-15.8"))
                .longitude(new BigDecimal("-47.8"))
                .areaTotalHectares(new BigDecimal("150.0"))
                .areaReservaHectares(new BigDecimal("30.0"))
                .areaCultivoHectares(new BigDecimal("120.0"))
                .emCultivo(true)
                .culturaAtual("Milho")
                .tipoSolo("Arenoso")
                .irrigacaoAtiva(false)
                .dataReferencia(LocalDate.of(2026, 6, 1))
                .observacoes("Area com potencial para expansão")
                .usuario(usuarioAdmin)
                .build();
        terrenoRepository.save(terreno2);
        log.info("Terreno criado: ID={}, Nome={}, Usuario={}", terreno2.getId(), terreno2.getNome(), usuarioAdmin.getCpf());
        
        // Terreno adicional do usuário comum
        Terreno terreno3 = Terreno.builder()
                .nome("Lavoura Norte")
                .descricao("Talhao principal para cultivo de soja")
                .latitude(new BigDecimal("-15.75"))
                .longitude(new BigDecimal("-47.95"))
                .areaTotalHectares(new BigDecimal("50.0"))
                .areaReservaHectares(new BigDecimal("10.0"))
                .areaCultivoHectares(new BigDecimal("35.0"))
                .emCultivo(true)
                .culturaAtual("Soja")
                .tipoSolo("Argiloso")
                .irrigacaoAtiva(true)
                .dataReferencia(LocalDate.of(2026, 6, 1))
                .observacoes("Monitoramento satelital ativo")
                .usuario(usuarioComum)
                .build();
        terrenoRepository.save(terreno3);
        log.info("Terreno criado: ID={}, Nome={}, Usuario={}", terreno3.getId(), terreno3.getNome(), usuarioComum.getCpf());
    }
}
