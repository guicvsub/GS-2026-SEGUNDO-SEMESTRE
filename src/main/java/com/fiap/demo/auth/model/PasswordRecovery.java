package com.fiap.demo.auth.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "password_recoveries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    @Builder.Default
    private Integer tentativasFalhas = 0;

    @Column
    private LocalDateTime bloqueadoAte;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public boolean estaBloqueado(LocalDateTime agora) {
        return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
    }

    public boolean estaExpirado(LocalDateTime agora) {
        return expiraEm != null && agora.isAfter(expiraEm);
    }
}
