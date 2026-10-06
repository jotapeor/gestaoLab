package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.TipoProjeto;

import java.time.LocalDateTime;

public record ProjetoResponse(
        Long id,
        String titulo,
        TipoProjeto tipo,
        OrientadorResumo orientador,
        String descricao,
        boolean ativo,
        LocalDateTime dataCadastro,
        int quantidadeParticipantes
) {
    public record OrientadorResumo(Long id, String nome) {}
}
