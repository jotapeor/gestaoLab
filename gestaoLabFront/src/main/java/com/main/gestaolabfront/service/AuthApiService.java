package com.main.gestaolabfront.service;

import com.main.gestaolabfront.dto.LoginResponse;
import com.main.gestaolabfront.exception.TokenExpiradoException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AuthApiService {

    private final RestClient restClient;

    public AuthApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    public LoginResponse logar(String email, String senha) {
        return restClient.post()
                .uri("/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .body(LoginResponse.class);
    }

    public LoginResponse trocarSenha(String senhaAtual, String novaSenha) {
        try {
            return restClient.post()
                    .uri("/autenticar/trocar-senha")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("senhaAtual", senhaAtual, "novaSenha", novaSenha))
                    .retrieve()
                    .body(LoginResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new TokenExpiradoException();
        }
    }
}
