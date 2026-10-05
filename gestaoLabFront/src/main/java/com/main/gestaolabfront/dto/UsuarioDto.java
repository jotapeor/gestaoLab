package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;

public record UsuarioDto(
        Long id,
        String nome,
        String matricula,
        String email,
        String perfil,
        Boolean ativo,
        Boolean primeiroAcesso,
        LocalDateTime dataCriacao,
        CursoSetorDto cursoSetor,
        ResponsavelDto responsavel
) {
    public record CursoSetorDto(Long id, String nome) {}
    public record ResponsavelDto(Long id, String nome) {}
}
