package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;

import java.time.LocalDateTime;

public record ReservaLaboratorioResponse(
        Long id,
        LaboratorioResumo laboratorio,
        UsuarioResumo usuario,
        ProjetoResumo projeto,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String motivo,
        StatusReserva status,
        LocalDateTime dataCriacao,
        UsuarioResumo canceladoPor,
        LocalDateTime dataCancelamento,
        String motivoCancelamento
) {
    public record LaboratorioResumo(Long id, String nome, String localizacao, Integer capacidade) {}
    public record UsuarioResumo(Long id, String nome) {}
    public record ProjetoResumo(Long id, String titulo) {}

    public static ReservaLaboratorioResponse de(ReservaLaboratorio r) {
        return new ReservaLaboratorioResponse(
                r.getId(),
                new LaboratorioResumo(r.getLaboratorio().getId(), r.getLaboratorio().getNome(),
                        r.getLaboratorio().getLocalizacao(), r.getLaboratorio().getCapacidade()),
                new UsuarioResumo(r.getUsuario().getId(), r.getUsuario().getNome()),
                r.getProjeto() != null
                        ? new ProjetoResumo(r.getProjeto().getId(), r.getProjeto().getTitulo())
                        : null,
                r.getDataInicio(),
                r.getDataFim(),
                r.getMotivo(),
                r.getStatus(),
                r.getDataCriacao(),
                r.getCanceladoPor() != null
                        ? new UsuarioResumo(r.getCanceladoPor().getId(), r.getCanceladoPor().getNome())
                        : null,
                r.getDataCancelamento(),
                r.getMotivoCancelamento()
        );
    }
}
