package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaLaboratorioRepository extends JpaRepository<ReservaLaboratorio, Long> {

    @Query(value = "SELECT r FROM ReservaLaboratorio r " +
                   "LEFT JOIN FETCH r.laboratorio " +
                   "LEFT JOIN FETCH r.usuario " +
                   "LEFT JOIN FETCH r.projeto " +
                   "WHERE (:laboratorioId IS NULL OR r.laboratorio.id = :laboratorioId) " +
                   "AND (:usuarioId IS NULL OR r.usuario.id = :usuarioId) " +
                   "AND (:status IS NULL OR r.status = :status) " +
                   "AND (:de IS NULL OR r.dataInicio >= :de) " +
                   "AND (:ate IS NULL OR r.dataFim <= :ate) " +
                   "ORDER BY r.dataInicio DESC",
           countQuery = "SELECT COUNT(r) FROM ReservaLaboratorio r " +
                        "WHERE (:laboratorioId IS NULL OR r.laboratorio.id = :laboratorioId) " +
                        "AND (:usuarioId IS NULL OR r.usuario.id = :usuarioId) " +
                        "AND (:status IS NULL OR r.status = :status) " +
                        "AND (:de IS NULL OR r.dataInicio >= :de) " +
                        "AND (:ate IS NULL OR r.dataFim <= :ate)")
    Page<ReservaLaboratorio> findWithFilters(
            @Param("laboratorioId") Long laboratorioId,
            @Param("usuarioId") Long usuarioId,
            @Param("status") StatusReserva status,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable
    );

    @Query(value = "SELECT r FROM ReservaLaboratorio r " +
                   "LEFT JOIN FETCH r.laboratorio " +
                   "LEFT JOIN FETCH r.usuario " +
                   "LEFT JOIN FETCH r.projeto " +
                   "WHERE (r.usuario.id = :professorId " +
                   "    OR (r.projeto IS NOT NULL AND r.projeto.orientador IS NOT NULL " +
                   "        AND r.projeto.orientador.id = :professorId)) " +
                   "AND (:laboratorioId IS NULL OR r.laboratorio.id = :laboratorioId) " +
                   "AND (:status IS NULL OR r.status = :status) " +
                   "AND (:de IS NULL OR r.dataInicio >= :de) " +
                   "AND (:ate IS NULL OR r.dataFim <= :ate) " +
                   "ORDER BY r.dataInicio DESC",
           countQuery = "SELECT COUNT(r) FROM ReservaLaboratorio r " +
                        "WHERE (r.usuario.id = :professorId " +
                        "    OR (r.projeto IS NOT NULL AND r.projeto.orientador IS NOT NULL " +
                        "        AND r.projeto.orientador.id = :professorId)) " +
                        "AND (:laboratorioId IS NULL OR r.laboratorio.id = :laboratorioId) " +
                        "AND (:status IS NULL OR r.status = :status) " +
                        "AND (:de IS NULL OR r.dataInicio >= :de) " +
                        "AND (:ate IS NULL OR r.dataFim <= :ate)")
    Page<ReservaLaboratorio> findWithFiltersParaProfessor(
            @Param("professorId") Long professorId,
            @Param("laboratorioId") Long laboratorioId,
            @Param("status") StatusReserva status,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable
    );

    @Query("SELECT r FROM ReservaLaboratorio r " +
           "LEFT JOIN FETCH r.laboratorio LEFT JOIN FETCH r.usuario LEFT JOIN FETCH r.projeto " +
           "LEFT JOIN FETCH r.canceladoPor " +
           "WHERE r.id = :id")
    Optional<ReservaLaboratorio> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM ReservaLaboratorio r " +
           "WHERE r.laboratorio.id = :labId " +
           "AND r.status = com.main.gestaolabback.model.StatusReserva.CONFIRMADA " +
           "AND r.dataInicio < :dataFim AND r.dataFim > :dataInicio")
    List<ReservaLaboratorio> findConfirmadasSobrepostas(
            @Param("labId") Long labId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    @Query("SELECT r FROM ReservaLaboratorio r " +
           "WHERE r.usuario.id = :usuarioId " +
           "AND r.status = com.main.gestaolabback.model.StatusReserva.CONFIRMADA " +
           "AND r.dataInicio < :dataFim AND r.dataFim > :dataInicio " +
           "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    List<ReservaLaboratorio> findConfirmadasDoUsuarioSobrepostas(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT r FROM ReservaLaboratorio r " +
           "LEFT JOIN FETCH r.usuario LEFT JOIN FETCH r.projeto " +
           "WHERE r.laboratorio.id = :labId " +
           "AND r.status = com.main.gestaolabback.model.StatusReserva.CONFIRMADA " +
           "AND r.dataInicio < :ate AND r.dataFim > :de " +
           "ORDER BY r.dataInicio")
    List<ReservaLaboratorio> findConfirmadasParaAgenda(
            @Param("labId") Long labId,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate
    );
}
