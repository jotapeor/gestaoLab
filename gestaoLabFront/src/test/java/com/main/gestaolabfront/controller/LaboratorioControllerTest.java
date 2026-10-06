package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.dto.LaboratorioDto;
import com.main.gestaolabfront.service.LaboratorioApiService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {LaboratorioController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class LaboratorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaboratorioApiService laboratorioApiService;

    @Test
    void listar_semSessao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/laboratorios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listar_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/laboratorios")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void listar_coordenador_retornaOk() throws Exception {
        when(laboratorioApiService.listar(any(), any()))
                .thenReturn(List.of(new LaboratorioDto(1L, "Lab A", "Bloco 1", 30, true, null)));
        mockMvc.perform(get("/laboratorios")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("laboratorios/lista"));
    }

    @Test
    void salvar_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(laboratorioApiService).criar(any(), any(), any());
        mockMvc.perform(post("/laboratorios/salvar")
                        .param("nome", "Lab A")
                        .param("localizacao", "Bloco 1")
                        .param("capacidadeStr", "30")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratorios"))
                .andExpect(flash().attribute("mensagemSucesso", "Laboratório cadastrado com sucesso!"));
    }

    @Test
    void salvar_nomeDuplicado_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Já existe um laboratório com o nome \\\"Lab A\\\".\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(laboratorioApiService).criar(any(), any(), any());
        mockMvc.perform(post("/laboratorios/salvar")
                        .param("nome", "Lab A")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratorios/novo"))
                .andExpect(flash().attributeExists("formError"));
    }

    @Test
    void salvar_nomeDuplicadoComAspas_exibeMensagemCompleta() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Já existe um laboratório com o nome \\\"Lab A\\\".\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(laboratorioApiService).criar(any(), any(), any());
        mockMvc.perform(post("/laboratorios/salvar")
                        .param("nome", "Lab A")
                        .param("localizacao", "Bloco 2")
                        .param("capacidadeStr", "20")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratorios/novo"))
                .andExpect(flash().attribute("formError",
                        "Já existe um laboratório com o nome \"Lab A\"."))
                .andExpect(flash().attribute("formNome", "Lab A"))
                .andExpect(flash().attribute("formLocalizacao", "Bloco 2"))
                .andExpect(flash().attribute("formCapacidade", "20"));
    }

    @Test
    void novoForm_comFlashError_mantemValoresNoFormulario() throws Exception {
        mockMvc.perform(get("/laboratorios/novo")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false")
                        .flashAttr("formNome", "Lab A")
                        .flashAttr("formLocalizacao", "Bloco 2")
                        .flashAttr("formCapacidade", "20")
                        .flashAttr("formError", "Já existe um laboratório com o nome \"Lab A\"."))
                .andExpect(status().isOk())
                .andExpect(view().name("laboratorios/form"))
                .andExpect(model().attribute("formNome", "Lab A"))
                .andExpect(model().attribute("formLocalizacao", "Bloco 2"))
                .andExpect(model().attribute("formCapacidade", "20"))
                .andExpect(model().attribute("formError", "Já existe um laboratório com o nome \"Lab A\"."));
    }

    @Test
    void salvar_professor_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/laboratorios/salvar")
                        .param("nome", "Lab X")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void inativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(laboratorioApiService).inativar(any());
        mockMvc.perform(post("/laboratorios/1/inativar")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratorios"))
                .andExpect(flash().attribute("mensagemSucesso", "Laboratório inativado com sucesso!"));
    }

    @Test
    void reativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(laboratorioApiService).reativar(any());
        mockMvc.perform(post("/laboratorios/1/reativar")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratorios"))
                .andExpect(flash().attribute("mensagemSucesso", "Laboratório reativado com sucesso!"));
    }
}
