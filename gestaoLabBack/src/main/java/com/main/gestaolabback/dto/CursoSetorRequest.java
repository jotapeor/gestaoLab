package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.TipoCursoSetor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CursoSetorRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,
        TipoCursoSetor tipo,
        Boolean ativo
) {}
