package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.Projeto;
import com.main.gestaolabback.model.TipoProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query(value = "SELECT p FROM Projeto p LEFT JOIN FETCH p.orientador " +
                   "WHERE (:busca IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
                   "AND (:tipo IS NULL OR p.tipo = :tipo) " +
                   "AND (:orientadorId IS NULL OR p.orientador.id = :orientadorId) " +
                   "AND (:participanteId IS NULL OR EXISTS (SELECT u FROM p.usuarios u WHERE u.id = :participanteId)) " +
                   "AND (:ativo IS NULL OR p.ativo = :ativo) " +
                   "ORDER BY p.titulo",
           countQuery = "SELECT COUNT(p) FROM Projeto p " +
                        "WHERE (:busca IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
                        "AND (:tipo IS NULL OR p.tipo = :tipo) " +
                        "AND (:orientadorId IS NULL OR p.orientador.id = :orientadorId) " +
                        "AND (:participanteId IS NULL OR EXISTS (SELECT u FROM p.usuarios u WHERE u.id = :participanteId)) " +
                        "AND (:ativo IS NULL OR p.ativo = :ativo)")
    Page<Projeto> findWithFilters(
            @Param("busca") String busca,
            @Param("tipo") TipoProjeto tipo,
            @Param("orientadorId") Long orientadorId,
            @Param("participanteId") Long participanteId,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN FETCH p.orientador " +
           "LEFT JOIN FETCH p.usuarios u LEFT JOIN FETCH u.cursoSetor " +
           "WHERE p.id = :id")
    Optional<Projeto> findByIdWithParticipantes(@Param("id") Long id);

    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.orientador " +
           "WHERE p.orientador.id = :orientadorId AND p.ativo = true ORDER BY p.titulo")
    List<Projeto> findMeusByOrientadorId(@Param("orientadorId") Long orientadorId);

    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN FETCH p.orientador " +
           "JOIN p.usuarios u WHERE u.id = :usuarioId AND p.ativo = true ORDER BY p.titulo")
    List<Projeto> findActiveByParticipanteId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.projetos p WHERE p.id = :projetoId")
    long countParticipantes(@Param("projetoId") Long projetoId);
}
