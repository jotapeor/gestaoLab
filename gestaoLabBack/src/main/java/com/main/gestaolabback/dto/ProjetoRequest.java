package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.TipoProjeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjetoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        TipoProjeto tipo,

        Long orientadorId,

        String descricao
) {}
