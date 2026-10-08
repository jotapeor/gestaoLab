package com.main.gestaolabback.dto;

public record DisponibilidadeResponse(
        boolean disponivel,
        Integer vagasRestantes,
        String mensagem
) {}
