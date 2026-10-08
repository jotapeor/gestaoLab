package com.main.gestaolabback.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AgendaLaboratorioResponse(
        Integer capacidade,
        List<BlocoAgenda> blocos
) {
    public record BlocoAgenda(
            Long reservaId,
            LocalDateTime inicio,
            LocalDateTime fim,
            String nomeUsuario,
            String nomeProjeto,
            int ocupacao
    ) {}
}
