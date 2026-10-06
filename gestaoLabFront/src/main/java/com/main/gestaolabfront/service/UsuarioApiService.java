package com.main.gestaolabfront.service;

import com.main.gestaolabfront.dto.UsuarioDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioApiService {

    private final RestClient restClient;

    public UsuarioApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listar(String busca, String perfil, Long cursoSetorId,
                                      Boolean ativo, int page) {
        return restClient.get()
                .uri(b -> b.path("/usuarios")
                        .queryParamIfPresent("busca", Optional.ofNullable(busca).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("perfil", Optional.ofNullable(perfil).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("cursoSetorId", Optional.ofNullable(cursoSetorId))
                        .queryParamIfPresent("ativo", Optional.ofNullable(ativo))
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(Map.class);
    }

    public UsuarioDto buscarPorId(Long id) {
        return restClient.get()
                .uri("/usuarios/{id}", id)
                .retrieve()
                .body(UsuarioDto.class);
    }

    public void criar(Map<String, Object> body) {
        restClient.post()
                .uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizar(Long id, Map<String, Object> body) {
        restClient.put()
                .uri("/usuarios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void inativar(Long id) {
        restClient.patch()
                .uri("/usuarios/{id}/inativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void reativar(Long id) {
        restClient.patch()
                .uri("/usuarios/{id}/reativar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void redefinirSenha(Long id, String novaSenhaProvisoria) {
        Map<String, String> body = new HashMap<>();
        body.put("novaSenhaProvisoria", novaSenhaProvisoria);
        restClient.patch()
                .uri("/usuarios/{id}/redefinir-senha", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarParticipantesDisponiveis(String busca) {
        return restClient.get()
                .uri(b -> b.path("/usuarios/participantes-disponiveis")
                        .queryParamIfPresent("busca", Optional.ofNullable(busca).filter(s -> !s.isBlank()))
                        .build())
                .retrieve()
                .body(List.class);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarResponsaveis() {
        return restClient.get()
                .uri("/usuarios/responsaveis")
                .retrieve()
                .body(List.class);
    }

    public UsuarioDto me() {
        return restClient.get()
                .uri("/usuarios/me")
                .retrieve()
                .body(UsuarioDto.class);
    }

    public UsuarioDto atualizarMe(String nome) {
        return restClient.put()
                .uri("/usuarios/me")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("nome", nome))
                .retrieve()
                .body(UsuarioDto.class);
    }
}
