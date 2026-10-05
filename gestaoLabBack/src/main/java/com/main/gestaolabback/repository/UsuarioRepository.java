package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    long countByPerfilAndAtivo(PerfilUsuario perfil, boolean ativo);

    List<Usuario> findByPerfilInAndAtivoTrueOrderByNome(List<PerfilUsuario> perfis);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cursoSetor LEFT JOIN FETCH u.responsavel WHERE " +
           "(:busca IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "  OR (:busca IS NOT NULL AND u.matricula IS NOT NULL AND LOWER(u.matricula) LIKE LOWER(CONCAT('%', :busca, '%')))) AND " +
           "(:perfil IS NULL OR u.perfil = :perfil) AND " +
           "(:cursoSetorId IS NULL OR u.cursoSetor.id = :cursoSetorId) AND " +
           "(:ativo IS NULL OR u.ativo = :ativo) " +
           "ORDER BY u.nome")
    Page<Usuario> findWithFilters(
            @Param("busca") String busca,
            @Param("perfil") PerfilUsuario perfil,
            @Param("cursoSetorId") Long cursoSetorId,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cursoSetor LEFT JOIN FETCH u.responsavel WHERE " +
           "u.responsavel.id = :responsavelId AND " +
           "(:busca IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "  OR (:busca IS NOT NULL AND u.matricula IS NOT NULL AND LOWER(u.matricula) LIKE LOWER(CONCAT('%', :busca, '%')))) AND " +
           "(:perfil IS NULL OR u.perfil = :perfil) AND " +
           "(:cursoSetorId IS NULL OR u.cursoSetor.id = :cursoSetorId) AND " +
           "(:ativo IS NULL OR u.ativo = :ativo) " +
           "ORDER BY u.nome")
    Page<Usuario> findByResponsavelWithFilters(
            @Param("responsavelId") Long responsavelId,
            @Param("busca") String busca,
            @Param("perfil") PerfilUsuario perfil,
            @Param("cursoSetorId") Long cursoSetorId,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );
}
