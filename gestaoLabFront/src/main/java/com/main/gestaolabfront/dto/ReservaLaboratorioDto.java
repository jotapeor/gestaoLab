package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;

public record ReservaLaboratorioDto(
        Long id,
        LaboratorioResumo laboratorio,
        UsuarioResumo usuario,
        ProjetoResumo projeto,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String motivo,
        String status,
        LocalDateTime dataCriacao,
        UsuarioResumo canceladoPor,
        LocalDateTime dataCancelamento,
        String motivoCancelamento
) {
    public record LaboratorioResumo(Long id, String nome, String localizacao, Integer capacidade) {}
    public record UsuarioResumo(Long id, String nome) {}
    public record ProjetoResumo(Long id, String titulo) {}
}
