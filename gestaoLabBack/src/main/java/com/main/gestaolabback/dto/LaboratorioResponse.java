package com.main.gestaolabback.dto;

import java.time.LocalDateTime;

public record LaboratorioResponse(
        Long id,
        String nome,
        String localizacao,
        Integer capacidade,
        boolean ativo,
        LocalDateTime dataCadastro
) {}
