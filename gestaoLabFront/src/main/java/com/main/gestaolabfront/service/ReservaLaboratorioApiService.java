package com.main.gestaolabfront.service;

import com.main.gestaolabfront.dto.AgendaDto;
import com.main.gestaolabfront.dto.DisponibilidadeDto;
import com.main.gestaolabfront.dto.PaginaResponse;
import com.main.gestaolabfront.dto.ReservaLaboratorioDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservaLaboratorioApiService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RestClient restClient;

    public ReservaLaboratorioApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PaginaResponse<ReservaLaboratorioDto> listar(Long laboratorioId, Long usuarioId,
                                                         String status, LocalDateTime de,
                                                         LocalDateTime ate, int page) {
        return restClient.get()
                .uri(b -> b.path("/reservas")
                        .queryParamIfPresent("laboratorioId", Optional.ofNullable(laboratorioId))
                        .queryParamIfPresent("usuarioId", Optional.ofNullable(usuarioId))
                        .queryParamIfPresent("status", Optional.ofNullable(status).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("de", Optional.ofNullable(de).map(ISO::format))
                        .queryParamIfPresent("ate", Optional.ofNullable(ate).map(ISO::format))
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PaginaResponse<ReservaLaboratorioDto>>() {});
    }

    public ReservaLaboratorioDto buscarPorId(Long id) {
        return restClient.get()
                .uri("/reservas/{id}", id)
                .retrieve()
                .body(ReservaLaboratorioDto.class);
    }

    public ReservaLaboratorioDto criar(Long laboratorioId, Long projetoId,
                                       LocalDateTime dataInicio, LocalDateTime dataFim,
                                       String motivo, Long usuarioId) {
        Map<String, Object> body = new HashMap<>();
        body.put("laboratorioId", laboratorioId);
        if (projetoId != null) body.put("projetoId", projetoId);
        body.put("dataInicio", dataInicio);
        body.put("dataFim", dataFim);
        if (motivo != null && !motivo.isBlank()) body.put("motivo", motivo);
        if (usuarioId != null) body.put("usuarioId", usuarioId);
        return restClient.post()
                .uri("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ReservaLaboratorioDto.class);
    }

    public ReservaLaboratorioDto cancelar(Long id, String motivo) {
        Map<String, Object> body = new HashMap<>();
        if (motivo != null && !motivo.isBlank()) body.put("motivo", motivo);
        return restClient.patch()
                .uri("/reservas/{id}/cancelar", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ReservaLaboratorioDto.class);
    }

    public DisponibilidadeDto verificarDisponibilidade(Long laboratorioId,
                                                        LocalDateTime inicio, LocalDateTime fim) {
        return restClient.get()
                .uri(b -> b.path("/reservas/disponibilidade")
                        .queryParam("laboratorioId", laboratorioId)
                        .queryParam("inicio", ISO.format(inicio))
                        .queryParam("fim", ISO.format(fim))
                        .build())
                .retrieve()
                .body(DisponibilidadeDto.class);
    }

    public AgendaDto agenda(Long laboratorioId, LocalDateTime de, LocalDateTime ate) {
        return restClient.get()
                .uri(b -> b.path("/laboratorios/{id}/agenda")
                        .queryParam("de", ISO.format(de))
                        .queryParam("ate", ISO.format(ate))
                        .build(laboratorioId))
                .retrieve()
                .body(AgendaDto.class);
    }
}
