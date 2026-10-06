package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjetoDetalheDto(
        Long id,
        String titulo,
        String tipo,
        OrientadorDto orientador,
        String descricao,
        Boolean ativo,
        LocalDateTime dataCadastro,
        List<ParticipanteDto> participantes
) {
    public record OrientadorDto(Long id, String nome) {}
    public record ParticipanteDto(Long id, String nome, String matricula, String cursoSetor, Boolean ativo) {}
}
