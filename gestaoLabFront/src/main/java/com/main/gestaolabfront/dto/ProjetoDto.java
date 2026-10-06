package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;

public record ProjetoDto(
        Long id,
        String titulo,
        String tipo,
        OrientadorDto orientador,
        String descricao,
        Boolean ativo,
        LocalDateTime dataCadastro,
        Integer quantidadeParticipantes
) {
    public record OrientadorDto(Long id, String nome) {}
}
