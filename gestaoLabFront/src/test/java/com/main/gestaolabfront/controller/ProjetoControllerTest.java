package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.dto.PaginaResponse;
import com.main.gestaolabfront.dto.ProjetoDetalheDto;
import com.main.gestaolabfront.dto.ProjetoDto;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProjetoController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class ProjetoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProjetoApiService projetoApiService;
    @MockitoBean private UsuarioApiService usuarioApiService;

    // ---- Listagem ----

    @Test
    void listar_semSessao_redirecionaLogin() throws Exception {
        mockMvc.perform(get("/projetos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listar_usuario_redirecionaMeusProjetos() throws Exception {
        when(projetoApiService.meusProjetos()).thenReturn(List.of());
        mockMvc.perform(get("/projetos")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos/meus"));
    }

    @Test
    void listar_coordenador_retornaOk() throws Exception {
        when(projetoApiService.listar(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 10, 0L, 1));
        when(usuarioApiService.listarResponsaveis()).thenReturn(List.of());
        mockMvc.perform(get("/projetos")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/lista"))
                .andExpect(model().attribute("isCoordenador", true));
    }

    @Test
    void listar_professor_retornaOk() throws Exception {
        when(projetoApiService.listar(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 10, 0L, 1));
        mockMvc.perform(get("/projetos")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/lista"))
                .andExpect(model().attribute("isCoordenador", false));
    }

    // ---- Meus projetos ----

    @Test
    void meusProjetos_usuario_retornaOk() throws Exception {
        when(projetoApiService.meusProjetos()).thenReturn(List.of());
        mockMvc.perform(get("/projetos/meus")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/meus-projetos"));
    }

    @Test
    void meusProjetos_semSessao_redirecionaLogin() throws Exception {
        mockMvc.perform(get("/projetos/meus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ---- Novo form ----

    @Test
    void novoForm_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/projetos/novo")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void novoForm_coordenador_retornaOk() throws Exception {
        when(usuarioApiService.listarResponsaveis()).thenReturn(List.of());
        mockMvc.perform(get("/projetos/novo")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/form"));
    }

    @Test
    void novoForm_professor_retornaOk() throws Exception {
        mockMvc.perform(get("/projetos/novo")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/form"));
    }

    // ---- Salvar ----

    @Test
    void salvar_coordenador_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(projetoApiService).criar(any());
        mockMvc.perform(post("/projetos/salvar")
                        .param("titulo", "TCC do Aluno")
                        .param("tipo", "TCC_I")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos"))
                .andExpect(flash().attribute("mensagemSucesso", "Projeto cadastrado com sucesso!"));
    }

    @Test
    void salvar_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/projetos/salvar")
                        .param("titulo", "TCC")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void salvar_erroApi_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.BadRequest.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"message\":\"Título é obrigatório.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(projetoApiService).criar(any());
        mockMvc.perform(post("/projetos/salvar")
                        .param("titulo", "")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos/novo"))
                .andExpect(flash().attributeExists("formError"));
    }

    // ---- Detalhe ----

    @Test
    void detalhe_retornaOk() throws Exception {
        when(projetoApiService.buscarPorId(1L)).thenReturn(projetoDetalheDto());
        mockMvc.perform(get("/projetos/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("projetos/detalhe"));
    }

    @Test
    void detalhe_semPermissao_redirecionaAcessoNegado() throws Exception {
        when(projetoApiService.buscarPorId(1L)).thenThrow(
                HttpClientErrorException.Forbidden.create(
                        HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, null, null));
        mockMvc.perform(get("/projetos/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void detalhe_naoEncontrado_redirecionaComErro() throws Exception {
        when(projetoApiService.buscarPorId(99L)).thenThrow(
                HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));
        mockMvc.perform(get("/projetos/99")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---- Inativar / Reativar ----

    @Test
    void inativar_coordenador_sucesso() throws Exception {
        doNothing().when(projetoApiService).inativar(1L);
        mockMvc.perform(post("/projetos/1/inativar")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos"))
                .andExpect(flash().attribute("mensagemSucesso", "Projeto inativado com sucesso!"));
    }

    @Test
    void inativar_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/projetos/1/inativar")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void reativar_professor_sucesso() throws Exception {
        doNothing().when(projetoApiService).reativar(1L);
        mockMvc.perform(post("/projetos/1/reativar")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos"))
                .andExpect(flash().attribute("mensagemSucesso", "Projeto reativado com sucesso!"));
    }

    // ---- Participantes ----

    @Test
    void adicionarParticipante_coordenador_sucesso() throws Exception {
        when(projetoApiService.adicionarParticipante(1L, 5L)).thenReturn(projetoDetalheDto());
        mockMvc.perform(post("/projetos/1/participantes")
                        .param("usuarioId", "5")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos/1"))
                .andExpect(flash().attribute("mensagemSucesso", "Participante adicionado com sucesso!"));
    }

    @Test
    void adicionarParticipante_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/projetos/1/participantes")
                        .param("usuarioId", "5")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void adicionarParticipante_duplicado_retornaErroNaRedirect() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Participante já está vinculado.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(projetoApiService).adicionarParticipante(1L, 5L);
        mockMvc.perform(post("/projetos/1/participantes")
                        .param("usuarioId", "5")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void removerParticipante_coordenador_sucesso() throws Exception {
        when(projetoApiService.removerParticipante(1L, 5L)).thenReturn(projetoDetalheDto());
        mockMvc.perform(post("/projetos/1/participantes/5/remover")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos/1"))
                .andExpect(flash().attribute("mensagemSucesso", "Participante removido com sucesso!"));
    }

    // ---- Helpers ----

    private static ProjetoDetalheDto projetoDetalheDto() {
        return new ProjetoDetalheDto(
                1L, "TCC I do Aluno", "TCC_I",
                new ProjetoDetalheDto.OrientadorDto(2L, "Prof. Silva"),
                "Descrição", true,
                LocalDateTime.of(2026, 10, 1, 10, 0),
                List.of()
        );
    }
}
