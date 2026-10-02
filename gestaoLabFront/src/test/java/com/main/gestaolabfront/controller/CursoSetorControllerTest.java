package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.service.CursoSetorApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CursoSetorController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class CursoSetorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CursoSetorApiService cursoSetorApiService;

    @Test
    void listar_semSessao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/cursos-setores"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listar_professsor_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/cursos-setores")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void listar_coordenador_retornaOk() throws Exception {
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of(
                Map.of("id", 1, "nome", "Biologia", "tipo", "CURSO", "ativo", true)
        ));
        mockMvc.perform(get("/cursos-setores")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos-setores/lista"));
    }

    @Test
    void salvar_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(cursoSetorApiService).criar(any(), any());
        mockMvc.perform(post("/cursos-setores/salvar")
                        .param("nome", "Biologia")
                        .param("tipo", "CURSO")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos-setores"))
                .andExpect(flash().attribute("mensagemSucesso", "Curso/Setor cadastrado com sucesso!"));
    }

    @Test
    void salvar_nomeDuplicado_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Já existe um curso/setor com o nome \\\"Biologia\\\".\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(cursoSetorApiService).criar(any(), any());
        mockMvc.perform(post("/cursos-setores/salvar")
                        .param("nome", "Biologia")
                        .param("tipo", "CURSO")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos-setores/novo"))
                .andExpect(flash().attributeExists("formError"));
    }

    @Test
    void salvar_nomeDuplicadoComAspas_exibeMensagemCompleta() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Já existe um curso/setor com o nome \\\"Biologia\\\".\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(cursoSetorApiService).criar(any(), any());
        mockMvc.perform(post("/cursos-setores/salvar")
                        .param("nome", "Biologia")
                        .param("tipo", "CURSO")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos-setores/novo"))
                .andExpect(flash().attribute("formError",
                        "Já existe um curso/setor com o nome \"Biologia\"."));
    }

    @Test
    void salvar_naoAutenticado_redirecionaParaLogin() throws Exception {
        mockMvc.perform(post("/cursos-setores/salvar")
                        .param("nome", "Bio")
                        .param("tipo", "CURSO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void salvar_professor_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/cursos-setores/salvar")
                        .param("nome", "Bio")
                        .param("tipo", "CURSO")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void inativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(cursoSetorApiService).inativar(any());
        mockMvc.perform(post("/cursos-setores/1/inativar")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos-setores"))
                .andExpect(flash().attribute("mensagemSucesso", "Curso/Setor inativado com sucesso!"));
    }

    @Test
    void reativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(cursoSetorApiService).reativar(any());
        mockMvc.perform(post("/cursos-setores/1/reativar")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos-setores"))
                .andExpect(flash().attribute("mensagemSucesso", "Curso/Setor reativado com sucesso!"));
    }
}
