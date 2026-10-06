package com.main.gestaolabfront.service;

import com.main.gestaolabfront.dto.ProjetoDetalheDto;
import com.main.gestaolabfront.dto.ProjetoDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjetoApiService {

    private final RestClient restClient;

    public ProjetoApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listar(String busca, String tipo, Long orientadorId,
                                      Long participanteId, Boolean ativo, int page) {
        return restClient.get()
                .uri(b -> b.path("/projetos")
                        .queryParamIfPresent("busca", Optional.ofNullable(busca).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("tipo", Optional.ofNullable(tipo).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("orientadorId", Optional.ofNullable(orientadorId))
                        .queryParamIfPresent("participanteId", Optional.ofNullable(participanteId))
                        .queryParamIfPresent("ativo", Optional.ofNullable(ativo))
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> meusProjetos() {
        return restClient.get()
                .uri("/projetos/meus")
                .retrieve()
                .body(List.class);
    }

    public ProjetoDetalheDto buscarPorId(Long id) {
        return restClient.get()
                .uri("/projetos/{id}", id)
                .retrieve()
                .body(ProjetoDetalheDto.class);
    }

    public void criar(Map<String, Object> body) {
        restClient.post()
                .uri("/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizar(Long id, Map<String, Object> body) {
        restClient.put()
                .uri("/projetos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void inativar(Long id) {
        restClient.patch()
                .uri("/projetos/{id}/inativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void reativar(Long id) {
        restClient.patch()
                .uri("/projetos/{id}/reativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public ProjetoDetalheDto adicionarParticipante(Long projetoId, Long usuarioId) {
        return restClient.post()
                .uri("/projetos/{id}/participantes", projetoId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("usuarioId", usuarioId))
                .retrieve()
                .body(ProjetoDetalheDto.class);
    }

    public ProjetoDetalheDto removerParticipante(Long projetoId, Long usuarioId) {
        return restClient.delete()
                .uri("/projetos/{id}/participantes/{usuarioId}", projetoId, usuarioId)
                .retrieve()
                .body(ProjetoDetalheDto.class);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarPorParticipante(Long participanteId) {
        Map<String, Object> pagina = restClient.get()
                .uri(b -> b.path("/projetos")
                        .queryParam("participanteId", participanteId)
                        .queryParam("ativo", true)
                        .queryParam("page", 0)
                        .build())
                .retrieve()
                .body(Map.class);
        return pagina != null ? (List<Map<String, Object>>) pagina.get("content") : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarPorOrientador(Long orientadorId) {
        Map<String, Object> pagina = restClient.get()
                .uri(b -> b.path("/projetos")
                        .queryParam("orientadorId", orientadorId)
                        .queryParam("ativo", true)
                        .queryParam("page", 0)
                        .build())
                .retrieve()
                .body(Map.class);
        return pagina != null ? (List<Map<String, Object>>) pagina.get("content") : List.of();
    }
}
