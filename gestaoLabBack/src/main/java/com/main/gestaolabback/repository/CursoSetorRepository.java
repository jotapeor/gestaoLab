package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.CursoSetor;
import com.main.gestaolabback.model.TipoCursoSetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoSetorRepository extends JpaRepository<CursoSetor, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query("SELECT cs FROM CursoSetor cs WHERE " +
           "(:nome IS NULL OR LOWER(cs.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:ativo IS NULL OR cs.ativo = :ativo) AND " +
           "(:tipo IS NULL OR cs.tipo = :tipo) " +
           "ORDER BY cs.nome")
    List<CursoSetor> findWithFilters(
            @Param("nome") String nome,
            @Param("ativo") Boolean ativo,
            @Param("tipo") TipoCursoSetor tipo
    );
}
