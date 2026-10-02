package com.main.gestaolabfront.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LaboratorioApiService {

    private final RestClient restClient;

    public LaboratorioApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listar(String nome, Boolean ativo) {
        return restClient.get()
                .uri(b -> b.path("/laboratorios")
                        .queryParamIfPresent("nome", Optional.ofNullable(nome).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("ativo", Optional.ofNullable(ativo))
                        .build())
                .retrieve()
                .body(List.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarPorId(Long id) {
        return restClient.get()
                .uri("/laboratorios/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    public void criar(String nome, String localizacao, Integer capacidade) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        if (localizacao != null && !localizacao.isBlank()) body.put("localizacao", localizacao);
        if (capacidade != null) body.put("capacidade", capacidade);
        restClient.post()
                .uri("/laboratorios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizar(Long id, String nome, String localizacao, Integer capacidade) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        if (localizacao != null && !localizacao.isBlank()) body.put("localizacao", localizacao);
        if (capacidade != null) body.put("capacidade", capacidade);
        restClient.put()
                .uri("/laboratorios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void inativar(Long id) {
        restClient.patch()
                .uri("/laboratorios/{id}/inativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void reativar(Long id) {
        restClient.patch()
                .uri("/laboratorios/{id}/reativar", id)
                .retrieve()
                .toBodilessEntity();
    }
}
