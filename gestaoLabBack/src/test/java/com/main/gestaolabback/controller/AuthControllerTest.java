package com.main.gestaolabback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.gestaolabback.dto.LoginRequest;
import com.main.gestaolabback.dto.TrocarSenhaRequest;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenService tokenService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        usuarioRepository.deleteAll();

        usuarioAtivo = new Usuario();
        usuarioAtivo.setNome("Admin");
        usuarioAtivo.setEmail("admin@gestaolab.local");
        usuarioAtivo.setSenha(passwordEncoder.encode("admin123"));
        usuarioAtivo.setPerfil(PerfilUsuario.COORDENADOR);
        usuarioAtivo.setAtivo(true);
        usuarioAtivo.setPrimeiroAcesso(true);
        usuarioAtivo = usuarioRepository.save(usuarioAtivo);

        usuarioInativo = new Usuario();
        usuarioInativo.setNome("Inativo");
        usuarioInativo.setEmail("inativo@gestaolab.local");
        usuarioInativo.setSenha(passwordEncoder.encode("senha123"));
        usuarioInativo.setPerfil(PerfilUsuario.USUARIO);
        usuarioInativo.setAtivo(false);
        usuarioInativo.setPrimeiroAcesso(true);
        usuarioInativo = usuarioRepository.save(usuarioInativo);
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @Test
    void logar_comSucesso_retornaToken() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin@gestaolab.local", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Admin"))
                .andExpect(jsonPath("$.perfil").value("COORDENADOR"))
                .andExpect(jsonPath("$.primeiroAcesso").value(true));
    }

    @Test
    void logar_senhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin@gestaolab.local", "senhaerrada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void logar_emailInexistente_retornaMesmaMensagem401() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("naoexiste@gestaolab.local", "qualquersenha"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void logar_usuarioInativo_retorna403() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("inativo@gestaolab.local", "senha123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Usuário inativo."));
    }

    @Test
    void logar_camposVazios_retorna400ComErrosDeCampo() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros").isMap());
    }

    @Test
    void logar_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("nao-e-email", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").exists());
    }

    @Test
    void rotaProtegida_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/autenticar/trocar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TrocarSenhaRequest("admin123", "novaSenha123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void trocarSenha_comSucesso_primeiroAcessoViraFalse() throws Exception {
        String token = tokenService.gerarToken(usuarioAtivo);

        mockMvc.perform(post("/api/autenticar/trocar-senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TrocarSenhaRequest("admin123", "novaSenha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.primeiroAcesso").value(false));

        Usuario updated = usuarioRepository.findById(usuarioAtivo.getId()).orElseThrow();
        assertThat(updated.isPrimeiroAcesso()).isFalse();
    }

    @Test
    void trocarSenha_senhaAtualErrada_retorna401() throws Exception {
        String token = tokenService.gerarToken(usuarioAtivo);

        mockMvc.perform(post("/api/autenticar/trocar-senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TrocarSenhaRequest("senhaErrada", "novaSenha123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Senha atual incorreta."));
    }

    @Test
    void trocarSenha_novaSenhaIgualAtual_retorna422() throws Exception {
        String token = tokenService.gerarToken(usuarioAtivo);

        mockMvc.perform(post("/api/autenticar/trocar-senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TrocarSenhaRequest("admin123", "admin123"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A nova senha não pode ser igual à senha atual."));
    }
}
