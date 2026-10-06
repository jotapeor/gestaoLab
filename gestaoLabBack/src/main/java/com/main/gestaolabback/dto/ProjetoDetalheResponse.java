package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.TipoProjeto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjetoDetalheResponse(
        Long id,
        String titulo,
        TipoProjeto tipo,
        OrientadorResumo orientador,
        String descricao,
        boolean ativo,
        LocalDateTime dataCadastro,
        List<ParticipanteResumo> participantes
) {
    public record OrientadorResumo(Long id, String nome) {}
    public record ParticipanteResumo(Long id, String nome, String matricula, String cursoSetor, boolean ativo) {}
}
