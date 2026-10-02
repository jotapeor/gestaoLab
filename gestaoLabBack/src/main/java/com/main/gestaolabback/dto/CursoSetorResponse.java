package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.TipoCursoSetor;

import java.time.LocalDateTime;

public record CursoSetorResponse(
        Long id,
        String nome,
        TipoCursoSetor tipo,
        boolean ativo,
        LocalDateTime dataCadastro
) {}
