package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.service.CursoSetorApiService;
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
import com.main.gestaolabfront.dto.UsuarioDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UsuarioController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private UsuarioApiService usuarioApiService;
    @MockitoBean private CursoSetorApiService cursoSetorApiService;
    @MockitoBean private ProjetoApiService projetoApiService;

    private static final Map<String, Object> USUARIO_MAP = usuarioMap();
    private static final Map<String, Object> PAGINA_MAP = paginaMap();

    private static Map<String, Object> usuarioMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", 1);
        m.put("nome", "Ana");
        m.put("email", "ana@lab.com");
        m.put("perfil", "USUARIO");
        m.put("ativo", true);
        m.put("matricula", "MAT001");
        m.put("cursoSetor", null);
        m.put("responsavel", null);
        m.put("dataCriacao", null);
        m.put("primeiroAcesso", true);
        return m;
    }

    private static Map<String, Object> paginaMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("conteudo", List.of(USUARIO_MAP));
        m.put("totalPaginas", 1);
        m.put("pagina", 0);
        return m;
    }

    // ---- listagem ----

    @Test
    void listar_semSessao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listar_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    @Test
    void listar_coordenador_retornaOk() throws Exception {
        when(usuarioApiService.listar(any(), any(), any(), any(), anyInt())).thenReturn(PAGINA_MAP);
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/usuarios")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"));
    }

    @Test
    void listar_professor_retornaOkSemBotaoNovo() throws Exception {
        when(usuarioApiService.listar(any(), any(), any(), any(), anyInt())).thenReturn(PAGINA_MAP);
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/usuarios")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(model().attribute("isCoordenador", false));
    }

    // ---- cadastro ----

    @Test
    void salvar_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(usuarioApiService).criar(any());
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Ana")
                        .param("email", "ana@lab.com")
                        .param("perfil", "USUARIO")
                        .param("senhaProvisoria", "senha123")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensagemSucesso", "Usuário cadastrado com sucesso!"));
    }

    @Test
    void salvar_emailDuplicado_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.Conflict.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"message\":\"Já existe um usuário com o e-mail \\\"ana@lab.com\\\".\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(usuarioApiService).criar(any());
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Ana")
                        .param("email", "ana@lab.com")
                        .param("perfil", "USUARIO")
                        .param("senhaProvisoria", "senha123")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/novo"))
                .andExpect(flash().attributeExists("formError"));
    }

    @Test
    void salvar_naoAutenticado_redirecionaParaLogin() throws Exception {
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Ana")
                        .param("email", "ana@lab.com")
                        .param("senhaProvisoria", "senha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void salvar_professor_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Ana")
                        .param("email", "ana@lab.com")
                        .param("senhaProvisoria", "senha123")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    // ---- acesso negado ----

    @Test
    void novoForm_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/usuarios/novo")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    // ---- redefinir senha ----

    @Test
    void redefinirSenha_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(usuarioApiService).redefinirSenha(anyLong(), any());
        mockMvc.perform(post("/usuarios/1/redefinir-senha")
                        .param("novaSenhaProvisoria", "novaSenha123")
                        .param("confirmacaoSenha", "novaSenha123")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensagemSucesso", "Senha redefinida com sucesso!"));
    }

    @Test
    void redefinirSenha_senhasNaoConferem_redirecionaComErro() throws Exception {
        mockMvc.perform(post("/usuarios/1/redefinir-senha")
                        .param("novaSenhaProvisoria", "novaSenha123")
                        .param("confirmacaoSenha", "outraSenha")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("errorMessage", "As senhas não conferem."));
    }

    // ---- inativar / reativar ----

    @Test
    void inativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(usuarioApiService).inativar(anyLong());
        mockMvc.perform(post("/usuarios/1/inativar")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensagemSucesso", "Usuário inativado com sucesso!"));
    }

    @Test
    void reativar_coordenador_redirecionaComSucesso() throws Exception {
        doNothing().when(usuarioApiService).reativar(anyLong());
        mockMvc.perform(post("/usuarios/1/reativar")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensagemSucesso", "Usuário reativado com sucesso!"));
    }

    // ---- detalhe ----

    @Test
    void detalhe_professor_retornaOk() throws Exception {
        when(usuarioApiService.buscarPorId(1L)).thenReturn(usuarioDtoSimples());
        mockMvc.perform(get("/usuarios/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/detalhe"));
    }

    @Test
    void detalhe_usuario_redirecionaAcessoNegado() throws Exception {
        mockMvc.perform(get("/usuarios/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acesso-negado"));
    }

    // ---- renderização de templates ----

    @Test
    void detalhe_renderizaComDataCriacao_semErro() throws Exception {
        when(usuarioApiService.buscarPorId(1L)).thenReturn(usuarioDtoComData());
        mockMvc.perform(get("/usuarios/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/detalhe"))
                .andExpect(content().string(containsString("04/10/2026 22:25")));
    }

    @Test
    void listar_renderizaListagemCompleta_semErro() throws Exception {
        Map<String, Object> pagina = new HashMap<>();
        pagina.put("conteudo", List.of(usuarioDtoComData()));
        pagina.put("totalPaginas", 1);
        pagina.put("pagina", 0);
        when(usuarioApiService.listar(any(), any(), any(), any(), anyInt())).thenReturn(pagina);
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/usuarios")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"));
    }

    @Test
    void novoForm_renderizaFormularioCadastro() throws Exception {
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of());
        when(usuarioApiService.listarResponsaveis()).thenReturn(List.of());
        mockMvc.perform(get("/usuarios/novo")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"));
    }

    @Test
    void editarForm_renderizaFormularioEdicao() throws Exception {
        when(usuarioApiService.buscarPorId(1L)).thenReturn(usuarioDtoComData());
        when(cursoSetorApiService.listar(any(), any(), any())).thenReturn(List.of());
        when(usuarioApiService.listarResponsaveis()).thenReturn(List.of());
        mockMvc.perform(get("/usuarios/editar/1")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"));
    }

    private static UsuarioDto usuarioDtoSimples() {
        return new UsuarioDto(1L, "Ana", "MAT001", "ana@lab.com", "USUARIO", true, false, null, null, null);
    }

    private static UsuarioDto usuarioDtoComData() {
        return new UsuarioDto(1L, "Prof. Silva", null, "silva@lab.com", "PROFESSOR", true, false,
                LocalDateTime.of(2026, 10, 4, 22, 25, 51), null, null);
    }
}
