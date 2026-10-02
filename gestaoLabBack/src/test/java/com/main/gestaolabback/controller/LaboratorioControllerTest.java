package com.main.gestaolabback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.gestaolabback.dto.LaboratorioRequest;
import com.main.gestaolabback.dto.LaboratorioResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.LaboratorioService;
import com.main.gestaolabback.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LaboratorioControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private LaboratorioService laboratorioService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenCoordenador;
    private String tokenProfessor;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenCoordenador = gerarToken(PerfilUsuario.COORDENADOR);
        tokenProfessor = gerarToken(PerfilUsuario.PROFESSOR);
        tokenUsuario = gerarToken(PerfilUsuario.USUARIO);

        LaboratorioResponse resp = new LaboratorioResponse(1L, "Lab A", "Bloco 1", 30, true, null);
        when(laboratorioService.listar(any(), any())).thenReturn(List.of(resp));
        when(laboratorioService.buscarPorId(anyLong())).thenReturn(resp);
        when(laboratorioService.criar(any())).thenReturn(resp);
        when(laboratorioService.atualizar(anyLong(), any())).thenReturn(resp);
        doNothing().when(laboratorioService).inativar(anyLong());
        doNothing().when(laboratorioService).reativar(anyLong());
    }

    private String gerarToken(PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail("lab_" + perfil.name() + "_" + System.nanoTime() + "@test.com");
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiroAcesso(false);
        return tokenService.gerarToken(usuarioRepository.save(u));
    }

    @Test
    void listar_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/laboratorios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_professorComFiltro_retorna200() throws Exception {
        mockMvc.perform(get("/api/laboratorios")
                        .param("nome", "Lab")
                        .param("ativo", "true")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/laboratorios/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    void criar_coordenador_retorna201() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab A", "Bloco 1", 30, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Lab A"));
    }

    @Test
    void criar_professor_retorna403() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab A", null, null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_usuario_retorna403() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab A", null, null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_nomeVazio_retorna400ComCampo() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void criar_capacidadeNegativa_retorna400() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab X", null, -5, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.capacidade").exists());
    }

    @Test
    void criar_nomeDuplicado_retorna409() throws Exception {
        when(laboratorioService.criar(any())).thenThrow(
                new ResponseStatusException(HttpStatusCode.valueOf(409),
                        "Já existe um laboratório com o nome \"Lab A\"."));
        mockMvc.perform(post("/api/laboratorios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab A", null, null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um laboratório com o nome \"Lab A\"."));
    }

    @Test
    void atualizar_coordenador_retorna200() throws Exception {
        mockMvc.perform(put("/api/laboratorios/1")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab Atualizado", "Bloco 2", 40, null))))
                .andExpect(status().isOk());
    }

    @Test
    void atualizar_usuario_retorna403() throws Exception {
        mockMvc.perform(put("/api/laboratorios/1")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab", null, null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(laboratorioService.buscarPorId(99L)).thenThrow(
                new ResponseStatusException(HttpStatusCode.valueOf(404), "Laboratório não encontrado."));
        mockMvc.perform(get("/api/laboratorios/99")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Laboratório não encontrado."));
    }

    @Test
    void inativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/laboratorios/1/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void inativar_professor_retorna403() throws Exception {
        mockMvc.perform(patch("/api/laboratorios/1/inativar")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isForbidden());
    }

    @Test
    void reativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/laboratorios/1/reativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/laboratorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LaboratorioRequest("Lab", null, null, null))))
                .andExpect(status().isUnauthorized());
    }
}
