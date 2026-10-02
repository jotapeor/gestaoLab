package com.main.gestaolabback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.gestaolabback.dto.CursoSetorRequest;
import com.main.gestaolabback.dto.CursoSetorResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.TipoCursoSetor;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.CursoSetorService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CursoSetorControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private CursoSetorService cursoSetorService;

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

        CursoSetorResponse resp = new CursoSetorResponse(1L, "Biologia", TipoCursoSetor.CURSO, true, null);
        when(cursoSetorService.listar(any(), any(), any())).thenReturn(List.of(resp));
        when(cursoSetorService.buscarPorId(anyLong())).thenReturn(resp);
        when(cursoSetorService.criar(any())).thenReturn(resp);
        when(cursoSetorService.atualizar(anyLong(), any())).thenReturn(resp);
        doNothing().when(cursoSetorService).inativar(anyLong());
        doNothing().when(cursoSetorService).reativar(anyLong());
    }

    private String gerarToken(PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail("teste_" + perfil.name() + "_" + System.nanoTime() + "@test.com");
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiroAcesso(false);
        return tokenService.gerarToken(usuarioRepository.save(u));
    }

    @Test
    void listar_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/cursos-setores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_comFiltros_professorRetorna200() throws Exception {
        mockMvc.perform(get("/api/cursos-setores")
                        .param("nome", "Bio")
                        .param("ativo", "true")
                        .param("tipo", "CURSO")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/cursos-setores/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    void criar_coordenador_retorna201() throws Exception {
        mockMvc.perform(post("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Biologia"));
    }

    @Test
    void criar_professor_retorna403() throws Exception {
        mockMvc.perform(post("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_usuario_retorna403() throws Exception {
        mockMvc.perform(post("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_nomeVazio_retorna400ComCampo() throws Exception {
        mockMvc.perform(post("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"tipo\":\"CURSO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void criar_nomeDuplicado_retorna409() throws Exception {
        when(cursoSetorService.criar(any())).thenThrow(
                new ResponseStatusException(HttpStatusCode.valueOf(409),
                        "Já existe um curso/setor com o nome \"Biologia\"."));
        mockMvc.perform(post("/api/cursos-setores")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um curso/setor com o nome \"Biologia\"."));
    }

    @Test
    void atualizar_coordenador_retorna200() throws Exception {
        mockMvc.perform(put("/api/cursos-setores/1")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Bio Atualizado", TipoCursoSetor.SETOR, null))))
                .andExpect(status().isOk());
    }

    @Test
    void atualizar_professor_retorna403() throws Exception {
        mockMvc.perform(put("/api/cursos-setores/1")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Bio", TipoCursoSetor.SETOR, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(cursoSetorService.buscarPorId(99L)).thenThrow(
                new ResponseStatusException(HttpStatusCode.valueOf(404), "Curso/Setor não encontrado."));
        mockMvc.perform(get("/api/cursos-setores/99")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Curso/Setor não encontrado."));
    }

    @Test
    void inativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/cursos-setores/1/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void inativar_professor_retorna403() throws Exception {
        mockMvc.perform(patch("/api/cursos-setores/1/inativar")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isForbidden());
    }

    @Test
    void reativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/cursos-setores/1/reativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void reativar_usuario_retorna403() throws Exception {
        mockMvc.perform(patch("/api/cursos-setores/1/reativar")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/cursos-setores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CursoSetorRequest("Bio", TipoCursoSetor.CURSO, null))))
                .andExpect(status().isUnauthorized());
    }
}
