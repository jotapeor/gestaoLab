package com.main.gestaolabback.dto;

public record ParticipanteDisponivelResponse(
        Long id,
        String nome,
        String matricula,
        String cursoSetor
) {}
