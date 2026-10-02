package com.main.gestaolabfront.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CursoSetorApiService {

    private final RestClient restClient;

    public CursoSetorApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listar(String nome, Boolean ativo, String tipo) {
        return restClient.get()
                .uri(b -> b.path("/cursos-setores")
                        .queryParamIfPresent("nome", Optional.ofNullable(nome).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("ativo", Optional.ofNullable(ativo))
                        .queryParamIfPresent("tipo", Optional.ofNullable(tipo).filter(s -> !s.isBlank()))
                        .build())
                .retrieve()
                .body(List.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarPorId(Long id) {
        return restClient.get()
                .uri("/cursos-setores/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    public void criar(String nome, String tipo) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("tipo", tipo);
        restClient.post()
                .uri("/cursos-setores")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizar(Long id, String nome, String tipo) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("tipo", tipo);
        restClient.put()
                .uri("/cursos-setores/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void inativar(Long id) {
        restClient.patch()
                .uri("/cursos-setores/{id}/inativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void reativar(Long id) {
        restClient.patch()
                .uri("/cursos-setores/{id}/reativar", id)
                .retrieve()
                .toBodilessEntity();
    }
}
