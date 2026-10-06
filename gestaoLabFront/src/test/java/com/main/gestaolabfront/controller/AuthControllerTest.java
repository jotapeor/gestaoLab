package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.dto.LoginResponse;
import com.main.gestaolabfront.dto.UsuarioDto;
import com.main.gestaolabfront.service.AuthApiService;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthApiService authApiService;

    @MockitoBean
    private UsuarioApiService usuarioApiService;

    @MockitoBean
    private ProjetoApiService projetoApiService;

    @Test
    void paginaDeLogin_abre() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void login_sucesso_redirecionaParaDashboard_eGuardaSessao() throws Exception {
        LoginResponse resposta = new LoginResponse("jwt-token", "Ana Lima", "COORDENADOR", false);
        when(authApiService.logar("ana@test.com", "senha123")).thenReturn(resposta);

        mockMvc.perform(post("/logar")
                        .param("email", "ana@test.com")
                        .param("senha", "senha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute("token", "jwt-token"))
                .andExpect(request().sessionAttribute("nome", "Ana Lima"))
                .andExpect(request().sessionAttribute("perfil", "COORDENADOR"))
                .andExpect(request().sessionAttribute("primeiroAcesso", "false"));
    }

    @Test
    void login_invalido_mostraMensagemDeErro() throws Exception {
        when(authApiService.logar(anyString(), anyString()))
                .thenThrow(HttpClientErrorException.Unauthorized.create(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY,
                        "{\"message\":\"Credenciais inválidas.\"}".getBytes(),
                        java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(post("/logar")
                        .param("email", "erro@test.com")
                        .param("senha", "errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("errorMessage", "Credenciais inválidas."));
    }

    @Test
    void acessoSemSessao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void primeiroAcesso_redirecionaParaTrocarSenha() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("nome", "Pedro")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trocar-senha"));
    }

    @Test
    void trocarSenha_confirmacaoDiferente_mostraErroSemChamarApi() throws Exception {
        mockMvc.perform(post("/fazer-trocar-senha")
                        .param("senhaAtual", "senhaAtual123")
                        .param("novaSenha", "novaSenha12")
                        .param("confirmacaoSenha", "diferente99")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("nome", "Pedro")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/trocar-senha"))
                .andExpect(model().attribute("errorMessage", "As senhas não conferem."));

        verify(authApiService, never()).trocarSenha(anyString(), anyString());
    }

    @Test
    void logout_invalidaSessaoERedirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/logout")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("nome", "Pedro")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private static UsuarioDto perfilMap(String nome) {
        return new UsuarioDto(1L, nome, null, "ana@test.com", "COORDENADOR", true, false, null, null, null);
    }

    @Test
    void login_sucesso_redirecionamento_naoContemJsessionid() throws Exception {
        when(authApiService.logar(anyString(), anyString()))
                .thenReturn(new LoginResponse("jwt-token", "Ana Lima", "COORDENADOR", false));

        mockMvc.perform(post("/logar")
                        .param("email", "ana@test.com")
                        .param("senha", "senha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Location"))
                        .doesNotContainIgnoringCase("jsessionid"));
    }

    @Test
    void meuPerfil_exibeDados() throws Exception {
        when(usuarioApiService.me()).thenReturn(perfilMap("Ana Lima"));
        mockMvc.perform(get("/meu-perfil")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/meu-perfil"));
    }

    @Test
    void salvarMeuPerfil_atualizaNomeSessao() throws Exception {
        when(usuarioApiService.atualizarMe("Novo Nome")).thenReturn(perfilMap("Novo Nome"));
        mockMvc.perform(post("/meu-perfil")
                        .param("nome", "Novo Nome")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/meu-perfil"))
                .andExpect(flash().attribute("mensagemSucesso", "Perfil atualizado com sucesso!"))
                .andExpect(request().sessionAttribute("nome", "Novo Nome"));
    }
}
