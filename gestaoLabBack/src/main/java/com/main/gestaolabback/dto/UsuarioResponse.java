package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.PerfilUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String matricula,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        boolean primeiroAcesso,
        LocalDateTime dataCriacao,
        CursoSetorResumo cursoSetor,
        UsuarioResumo responsavel
) {
    public record CursoSetorResumo(Long id, String nome) {}
    public record UsuarioResumo(Long id, String nome) {}
}
