package com.main.gestaolabback.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservaLaboratorioRequest(
        @NotNull(message = "Laboratório é obrigatório")
        Long laboratorioId,

        Long projetoId,

        @NotNull(message = "Data de início é obrigatória")
        LocalDateTime dataInicio,

        @NotNull(message = "Data de fim é obrigatória")
        LocalDateTime dataFim,

        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
        String motivo,

        Long usuarioId
) {}
