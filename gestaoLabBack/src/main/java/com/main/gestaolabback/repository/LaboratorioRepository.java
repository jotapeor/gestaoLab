package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query("SELECT l FROM Laboratorio l WHERE " +
           "(:nome IS NULL OR LOWER(l.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:ativo IS NULL OR l.ativo = :ativo) " +
           "ORDER BY l.nome")
    List<Laboratorio> findWithFilters(
            @Param("nome") String nome,
            @Param("ativo") Boolean ativo
    );
}
