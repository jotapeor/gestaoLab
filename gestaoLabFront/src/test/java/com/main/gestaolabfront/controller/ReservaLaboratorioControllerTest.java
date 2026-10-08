package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.config.SessionCheckInterceptor;
import com.main.gestaolabfront.config.WebConfig;
import com.main.gestaolabfront.dto.AgendaDto;
import com.main.gestaolabfront.dto.DisponibilidadeDto;
import com.main.gestaolabfront.dto.LaboratorioDto;
import com.main.gestaolabfront.dto.PaginaResponse;
import com.main.gestaolabfront.dto.ProjetoDto;
import com.main.gestaolabfront.dto.ReservaLaboratorioDto;
import com.main.gestaolabfront.dto.UsuarioDto;
import com.main.gestaolabfront.service.LaboratorioApiService;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.ReservaLaboratorioApiService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ReservaLaboratorioController.class, GlobalExceptionHandler.class})
@Import({SessionCheckInterceptor.class, WebConfig.class})
class ReservaLaboratorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ReservaLaboratorioApiService reservaApiService;
    @MockitoBean private LaboratorioApiService laboratorioApiService;
    @MockitoBean private ProjetoApiService projetoApiService;
    @MockitoBean private UsuarioApiService usuarioApiService;

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 9, 0);
    private static final LocalDateTime FIM    = LocalDateTime.of(2026, 11, 10, 11, 0);

    // ---------- lista ----------

    @Test
    void listar_semSessao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/reservas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listar_comSessao_retornaOk() throws Exception {
        when(reservaApiService.listar(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0L, 0));
        when(laboratorioApiService.listar(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/reservas")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/lista"));
    }

    // ---------- nova form ----------

    @Test
    void novaForm_comSessao_retornaFormulario() throws Exception {
        when(laboratorioApiService.listar(any(), any())).thenReturn(List.of(
                new LaboratorioDto(1L, "Lab A", null, 30, true, null)));

        mockMvc.perform(get("/reservas/nova")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/form"));
    }

    @Test
    void novaForm_semSessao_redirecionaLogin() throws Exception {
        mockMvc.perform(get("/reservas/nova"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // item 1: select de projeto renderizado no formulário
    @Test
    void novaForm_exibeSelectDeProjeto() throws Exception {
        when(laboratorioApiService.listar(any(), any())).thenReturn(List.of());
        when(projetoApiService.meusProjetos()).thenReturn(List.of(
                new ProjetoDto(1L, "Projeto ABC", "TCC_I", null, null, true, null, 3)));

        mockMvc.perform(get("/reservas/nova")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("projetoId")))
                .andExpect(content().string(containsString("Projeto ABC")));
    }

    // item 1: coordenador vê o campo de busca de usuário
    @Test
    void novaForm_coordenador_exibeBuscaDeUsuario() throws Exception {
        when(laboratorioApiService.listar(any(), any())).thenReturn(List.of());
        when(projetoApiService.meusProjetos()).thenReturn(List.of());

        mockMvc.perform(get("/reservas/nova")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("usuarioBusca")))
                .andExpect(content().string(containsString("usuarioId")));
    }

    // ---------- salvar ----------

    @Test
    void salvar_sucesso_redirecionaParaDetalhe() throws Exception {
        ReservaLaboratorioDto criada = reservaFake(99L);
        when(reservaApiService.criar(any(), any(), any(), any(), any(), any())).thenReturn(criada);

        mockMvc.perform(post("/reservas/salvar")
                        .param("laboratorioId", "1")
                        .param("dataInicio", "2026-11-10T09:00")
                        .param("dataFim", "2026-11-10T11:00")
                        .param("motivo", "Pesquisa")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/99"))
                .andExpect(flash().attribute("mensagemSucesso", "Reserva registrada com sucesso!"));
    }

    @Test
    void salvar_laboratorioLotado_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.UnprocessableEntity.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable", HttpHeaders.EMPTY,
                "{\"message\":\"Laboratório lotado neste horário: capacidade de 2 pessoas.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(reservaApiService).criar(any(), any(), any(), any(), any(), any());

        mockMvc.perform(post("/reservas/salvar")
                        .param("laboratorioId", "1")
                        .param("dataInicio", "2026-11-10T09:00")
                        .param("dataFim", "2026-11-10T11:00")
                        .param("motivo", "Pesquisa")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/nova"))
                .andExpect(flash().attributeExists("formError"));
    }

    @Test
    void salvar_semSessao_redirecionaLogin() throws Exception {
        mockMvc.perform(post("/reservas/salvar")
                        .param("laboratorioId", "1")
                        .param("dataInicio", "2026-11-10T09:00")
                        .param("dataFim", "2026-11-10T11:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ---------- detalhe ----------

    @Test
    void detalhe_encontrado_retornaView() throws Exception {
        when(reservaApiService.buscarPorId(1L)).thenReturn(reservaFake(1L));

        mockMvc.perform(get("/reservas/1")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/detalhe"));
    }

    @Test
    void detalhe_naoEncontrado_redirecionaComErro() throws Exception {
        when(reservaApiService.buscarPorId(anyLong()))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        mockMvc.perform(get("/reservas/99")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- cancelar ----------

    @Test
    void cancelar_sucesso_redirecionaComMensagem() throws Exception {
        when(reservaApiService.cancelar(eq(1L), any())).thenReturn(reservaFake(1L));

        mockMvc.perform(post("/reservas/1/cancelar")
                        .param("motivo", "Não preciso mais")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/1"))
                .andExpect(flash().attribute("mensagemSucesso", "Reserva cancelada com sucesso!"));
    }

    // item 2: motivo digitado chega na API
    @Test
    void cancelar_motivoEnviadoParaApi() throws Exception {
        when(reservaApiService.cancelar(eq(5L), eq("Manutenção urgente"))).thenReturn(reservaFake(5L));

        mockMvc.perform(post("/reservas/5/cancelar")
                        .param("motivo", "Manutenção urgente")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/5"));

        verify(reservaApiService).cancelar(5L, "Manutenção urgente");
    }

    @Test
    void cancelar_erroApi_redirecionaComErro() throws Exception {
        doThrow(HttpClientErrorException.UnprocessableEntity.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable", HttpHeaders.EMPTY,
                "{\"message\":\"Você só pode cancelar a reserva antes do início.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(reservaApiService).cancelar(anyLong(), any());

        mockMvc.perform(post("/reservas/1/cancelar")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/1"))
                .andExpect(flash().attribute("errorMessage",
                        "Você só pode cancelar a reserva antes do início."));
    }

    // ---------- agenda ----------

    @Test
    void agenda_retornaView() throws Exception {
        when(laboratorioApiService.buscarPorId(1L))
                .thenReturn(new LaboratorioDto(1L, "Lab A", null, 30, true, null));
        when(reservaApiService.agenda(eq(1L), any(), any()))
                .thenReturn(new AgendaDto(30, List.of()));

        mockMvc.perform(get("/reservas/agenda/1")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/agenda"))
                .andExpect(model().attributeExists("laboratorio", "agenda", "semanaInicio", "semanaFim"));
    }

    // item 4: ocupação renderizada no template da agenda
    @Test
    void agenda_exibeOcupacaoPorBloco() throws Exception {
        AgendaDto.BlocoAgenda bloco = new AgendaDto.BlocoAgenda(
                1L, INICIO, FIM, "João", "Projeto X", 2);
        when(laboratorioApiService.buscarPorId(1L))
                .thenReturn(new LaboratorioDto(1L, "Lab A", null, 2, true, null));
        when(reservaApiService.agenda(eq(1L), any(), any()))
                .thenReturn(new AgendaDto(2, List.of(bloco)));

        mockMvc.perform(get("/reservas/agenda/1")
                        .param("de", "2026-11-10")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2/2")));
    }

    @Test
    void agenda_labNaoEncontrado_redirecionaComErro() throws Exception {
        when(laboratorioApiService.buscarPorId(99L))
                .thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/reservas/agenda/99")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- disponibilidade (JSON endpoint) ----------

    @Test
    void disponibilidade_retornaJson() throws Exception {
        when(reservaApiService.verificarDisponibilidade(eq(1L), any(), any()))
                .thenReturn(new DisponibilidadeDto(true, 4, "Disponível – 4 vaga(s) restante(s)."));

        mockMvc.perform(get("/reservas/disponibilidade")
                        .param("laboratorioId", "1")
                        .param("inicio", "2026-11-10T09:00:00")
                        .param("fim", "2026-11-10T11:00:00")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "USUARIO")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    // item 3: busca de usuários (JSON) para coordenador
    @Test
    void buscarUsuarios_coordenador_retornaListaJson() throws Exception {
        UsuarioDto u = new UsuarioDto(5L, "Ana Lima", "2024001", "ana@lab.com",
                "USUARIO", true, false, null, null, null);
        when(usuarioApiService.listar(eq("ana"), any(), any(), eq(true), eq(0)))
                .thenReturn(new PaginaResponse<>(List.of(u), 0, 20, 1L, 1));

        mockMvc.perform(get("/reservas/buscar-usuarios")
                        .param("q", "ana")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana Lima"))
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void buscarUsuarios_naoCoord_retornaVazio() throws Exception {
        mockMvc.perform(get("/reservas/buscar-usuarios")
                        .param("q", "ana")
                        .sessionAttr("token", "jwt-valido")
                        .sessionAttr("perfil", "PROFESSOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    // ---------- helpers ----------

    private static ReservaLaboratorioDto reservaFake(Long id) {
        return new ReservaLaboratorioDto(
                id,
                new ReservaLaboratorioDto.LaboratorioResumo(1L, "Lab A", null, 30),
                new ReservaLaboratorioDto.UsuarioResumo(10L, "Aluno"),
                null,
                INICIO, FIM,
                "Pesquisa",
                "CONFIRMADA",
                LocalDateTime.of(2026, 11, 1, 8, 0),
                null, null, null
        );
    }
}
