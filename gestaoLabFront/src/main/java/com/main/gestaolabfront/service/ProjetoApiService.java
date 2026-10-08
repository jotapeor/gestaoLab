package com.main.gestaolabfront.service;

import com.main.gestaolabfront.dto.PaginaResponse;
import com.main.gestaolabfront.dto.ProjetoDetalheDto;
import com.main.gestaolabfront.dto.ProjetoDto;
import org.springframework.core.ParameterizedTypeReference;
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

    public PaginaResponse<ProjetoDto> listar(String busca, String tipo, Long orientadorId,
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
                .body(new ParameterizedTypeReference<PaginaResponse<ProjetoDto>>() {});
    }

    public List<ProjetoDto> meusProjetos() {
        return restClient.get()
                .uri("/projetos/meus")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProjetoDto>>() {});
    }

    public List<ProjetoDto> projetosDe(Long usuarioId) {
        return restClient.get()
                .uri("/projetos/usuario/{id}", usuarioId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProjetoDto>>() {});
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

    public List<ProjetoDto> listarPorParticipante(Long participanteId) {
        PaginaResponse<ProjetoDto> pagina = restClient.get()
                .uri(b -> b.path("/projetos")
                        .queryParam("participanteId", participanteId)
                        .queryParam("ativo", true)
                        .queryParam("page", 0)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PaginaResponse<ProjetoDto>>() {});
        return pagina != null ? pagina.conteudo() : List.of();
    }

    public List<ProjetoDto> listarPorOrientador(Long orientadorId) {
        PaginaResponse<ProjetoDto> pagina = restClient.get()
                .uri(b -> b.path("/projetos")
                        .queryParam("orientadorId", orientadorId)
                        .queryParam("ativo", true)
                        .queryParam("page", 0)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PaginaResponse<ProjetoDto>>() {});
        return pagina != null ? pagina.conteudo() : List.of();
    }
}
