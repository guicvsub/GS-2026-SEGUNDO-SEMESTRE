package com.fiap.demo.terreno.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fiap.demo.auth.model.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "terrenos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Terreno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Area total da propriedade em hectares. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal areaTotalHectares;

    /** Area de reserva legal / preservacao em hectares. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal areaReservaHectares = BigDecimal.ZERO;

    /** Area efetivamente plantada ou em cultivo em hectares. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal areaCultivoHectares = BigDecimal.ZERO;

    /** Indica se a area de cultivo esta ativa no momento. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emCultivo = false;

    @Column(length = 80)
    private String culturaAtual;

    @Column(length = 50)
    private String tipoSolo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean irrigacaoAtiva = false;

    /** Data de referencia do registro diario do terreno. */
    @Column(nullable = false)
    private LocalDate dataReferencia;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
