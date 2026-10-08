package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AgendaDto(Integer capacidade, List<BlocoAgenda> blocos) {
    public record BlocoAgenda(
            Long reservaId,
            LocalDateTime inicio,
            LocalDateTime fim,
            String nomeUsuario,
            String nomeProjeto
    ) {}
}
