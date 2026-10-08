package com.main.gestaolabback.dto;

import jakarta.validation.constraints.Size;

public record CancelarReservaRequest(
        @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
        String motivo
) {}
