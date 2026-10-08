package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.DisponibilidadeResponse;
import com.main.gestaolabback.dto.ReservaLaboratorioRequest;
import com.main.gestaolabback.dto.ReservaLaboratorioResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.ReservaLaboratorioService;
import com.main.gestaolabback.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservaLaboratorioControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private ReservaLaboratorioService reservaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenCoordenador;
    private String tokenProfessor;
    private String tokenUsuario;

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 9, 0);
    private static final LocalDateTime FIM    = LocalDateTime.of(2026, 11, 10, 11, 0);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenCoordenador = gerarToken(PerfilUsuario.COORDENADOR);
        tokenProfessor   = gerarToken(PerfilUsuario.PROFESSOR);
        tokenUsuario     = gerarToken(PerfilUsuario.USUARIO);

        ReservaLaboratorioResponse resp = reservaResponseFake();
        when(reservaService.listar(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(resp), PageRequest.of(0, 20), 1));
        when(reservaService.buscarPorId(anyLong(), any())).thenReturn(resp);
        when(reservaService.criar(any(), any())).thenReturn(resp);
        when(reservaService.cancelar(anyLong(), any(), any())).thenReturn(resp);
        when(reservaService.verificarDisponibilidade(anyLong(), any(), any(), any()))
                .thenReturn(new DisponibilidadeResponse(true, 5, "Disponível"));
    }

    private String gerarToken(PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail("reserva_" + perfil.name() + "_" + System.nanoTime() + "@test.com");
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiroAcesso(false);
        return tokenService.gerarToken(usuarioRepository.save(u));
    }

    // ---------- GET /api/reservas ----------

    @Test
    void listar_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/reservas")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET /api/reservas/{id} ----------

    @Test
    void buscarPorId_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/reservas/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscarPorId_naoEncontrado_retorna404() throws Exception {
        when(reservaService.buscarPorId(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(404), "Reserva não encontrada."));
        mockMvc.perform(get("/api/reservas/99")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reserva não encontrada."));
    }

    @Test
    void buscarPorId_semPermissao_retorna403() throws Exception {
        when(reservaService.buscarPorId(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado."));
        mockMvc.perform(get("/api/reservas/2")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /api/reservas ----------

    @Test
    void criar_usuario_retorna201() throws Exception {
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(1L, null, INICIO, FIM, "Motivo", null);
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void criar_semToken_retorna401() throws Exception {
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(1L, null, INICIO, FIM, "Motivo", null);
        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_semLaboratorio_retorna400() throws Exception {
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInicio\":\"2026-11-10T09:00:00\",\"dataFim\":\"2026-11-10T11:00:00\",\"motivo\":\"Teste\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.laboratorioId").exists());
    }

    @Test
    void criar_laboratorioInativo_retorna422() throws Exception {
        when(reservaService.criar(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Laboratório inativo."));
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(1L, null, INICIO, FIM, "Motivo", null);
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criar_laboratorioLotado_retorna422() throws Exception {
        when(reservaService.criar(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Laboratório lotado neste horário: capacidade de 2 pessoas."));
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(1L, null, INICIO, FIM, "Motivo", null);
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("lotado")));
    }

    @Test
    void criar_coordenadorComUsuarioId_retorna201() throws Exception {
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(1L, null, INICIO, FIM, "Motivo", 5L);
        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    // ---------- PATCH /api/reservas/{id}/cancelar ----------

    @Test
    void cancelar_comToken_retorna200() throws Exception {
        mockMvc.perform(patch("/api/reservas/1/cancelar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Não vou mais usar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA")); // service mock returns fake
    }

    @Test
    void cancelar_semToken_retorna401() throws Exception {
        mockMvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelar_jaEncerrada_retorna422() throws Exception {
        when(reservaService.cancelar(anyLong(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Não é possível cancelar uma reserva já encerrada."));
        mockMvc.perform(patch("/api/reservas/1/cancelar")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelar_semPermissao_retorna403() throws Exception {
        when(reservaService.cancelar(anyLong(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado."));
        mockMvc.perform(patch("/api/reservas/2/cancelar")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    // ---------- GET /api/reservas/disponibilidade ----------

    @Test
    void disponibilidade_disponivel_retorna200() throws Exception {
        mockMvc.perform(get("/api/reservas/disponibilidade")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .param("laboratorioId", "1")
                        .param("inicio", "2026-11-10T09:00:00")
                        .param("fim", "2026-11-10T11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void disponibilidade_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/reservas/disponibilidade")
                        .param("laboratorioId", "1")
                        .param("inicio", "2026-11-10T09:00:00")
                        .param("fim", "2026-11-10T11:00:00"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- helpers ----------

    private static ReservaLaboratorioResponse reservaResponseFake() {
        return new ReservaLaboratorioResponse(
                1L,
                new ReservaLaboratorioResponse.LaboratorioResumo(1L, "Lab A", "Bloco 1", 30),
                new ReservaLaboratorioResponse.UsuarioResumo(10L, "Aluno"),
                null,
                INICIO, FIM,
                "Motivo",
                StatusReserva.CONFIRMADA,
                LocalDateTime.of(2026, 11, 1, 8, 0),
                null, null, null
        );
    }
}
