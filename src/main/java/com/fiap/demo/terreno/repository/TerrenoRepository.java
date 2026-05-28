package com.fiap.demo.terreno.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fiap.demo.terreno.model.Terreno;

public interface TerrenoRepository extends JpaRepository<Terreno, Long> {

    List<Terreno> findByUsuarioId(Long usuarioId);

    List<Terreno> findByUsuario_Cpf(String cpf);

    Optional<Terreno> findByIdAndUsuario_Cpf(Long id, String cpf);
}
