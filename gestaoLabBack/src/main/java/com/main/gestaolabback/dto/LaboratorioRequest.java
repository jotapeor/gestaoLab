package com.main.gestaolabback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LaboratorioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,
        String localizacao,
        @Positive(message = "Capacidade deve ser maior que zero")
        Integer capacidade,
        Boolean ativo
) {}
