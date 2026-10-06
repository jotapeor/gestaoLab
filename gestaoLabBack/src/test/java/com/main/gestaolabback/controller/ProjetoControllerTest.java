package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.PaginaResponse;
import com.main.gestaolabback.dto.ProjetoDetalheResponse;
import com.main.gestaolabback.dto.ProjetoRequest;
import com.main.gestaolabback.dto.ProjetoResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.TipoProjeto;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.ProjetoService;
import com.main.gestaolabback.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjetoControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private ProjetoService projetoService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenCoordenador;
    private String tokenProfessor;
    private String tokenUsuario;

    private ProjetoResponse projetoResponse;
    private ProjetoDetalheResponse projetoDetalheResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenCoordenador = gerarToken(PerfilUsuario.COORDENADOR);
        tokenProfessor = gerarToken(PerfilUsuario.PROFESSOR);
        tokenUsuario = gerarToken(PerfilUsuario.USUARIO);

        ProjetoResponse.OrientadorResumo orientador = new ProjetoResponse.OrientadorResumo(2L, "Prof");
        projetoResponse = new ProjetoResponse(1L, "TCC A", TipoProjeto.TCC_I, orientador, null, true, LocalDateTime.now(), 0);
        ProjetoDetalheResponse.OrientadorResumo orientadorDetalhe = new ProjetoDetalheResponse.OrientadorResumo(2L, "Prof");
        projetoDetalheResponse = new ProjetoDetalheResponse(1L, "TCC A", TipoProjeto.TCC_I, orientadorDetalhe, null, true, LocalDateTime.now(), List.of());

        when(projetoService.listar(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(projetoResponse)));
        when(projetoService.buscarPorId(anyLong(), any())).thenReturn(projetoDetalheResponse);
        when(projetoService.criar(any(), any())).thenReturn(projetoResponse);
        when(projetoService.atualizar(anyLong(), any(), any())).thenReturn(projetoResponse);
        doNothing().when(projetoService).inativar(anyLong(), any());
        doNothing().when(projetoService).reativar(anyLong(), any());
        when(projetoService.adicionarParticipante(anyLong(), anyLong(), any())).thenReturn(projetoDetalheResponse);
        when(projetoService.removerParticipante(anyLong(), anyLong(), any())).thenReturn(projetoDetalheResponse);
        when(projetoService.meusProjetos(any())).thenReturn(List.of(projetoResponse));
    }

    private String gerarToken(PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail("proj_" + perfil.name() + "_" + System.nanoTime() + "@test.com");
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiroAcesso(false);
        return tokenService.gerarToken(usuarioRepository.save(u));
    }

    // --- GET /api/projetos ---

    @Test
    void listar_coordenador_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].titulo").value("TCC A"));
    }

    @Test
    void listar_professor_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void listar_usuario_retorna403() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/projetos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_comFiltros_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .param("busca", "TCC")
                        .param("tipo", "TCC_I")
                        .param("ativo", "true")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk());
    }

    // --- GET /api/projetos/meus ---

    @Test
    void meusProjetos_usuario_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos/meus")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("TCC A"));
    }

    @Test
    void meusProjetos_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/projetos/meus"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/projetos/{id} ---

    @Test
    void buscarPorId_coordenador_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos/1")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("TCC A"));
    }

    @Test
    void buscarPorId_usuario_retorna200() throws Exception {
        mockMvc.perform(get("/api/projetos/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_naoParticipante_retorna403() throws Exception {
        when(projetoService.buscarPorId(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado."));
        mockMvc.perform(get("/api/projetos/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(projetoService.buscarPorId(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(404), "Projeto não encontrado."));
        mockMvc.perform(get("/api/projetos/99")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado."));
    }

    // --- POST /api/projetos ---

    @Test
    void criar_coordenador_retorna201() throws Exception {
        ProjetoRequest req = new ProjetoRequest("TCC A", TipoProjeto.TCC_I, 2L, null);
        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("TCC A"));
    }

    @Test
    void criar_professor_retorna201() throws Exception {
        ProjetoRequest req = new ProjetoRequest("TCC A", TipoProjeto.TCC_I, null, null);
        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void criar_usuario_retorna403() throws Exception {
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, null, null);
        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_tituloVazio_retorna400() throws Exception {
        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"orientadorId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.titulo").exists());
    }

    // --- PATCH inativar / reativar ---

    @Test
    void inativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/projetos/1/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void inativar_usuario_retorna403() throws Exception {
        mockMvc.perform(patch("/api/projetos/1/inativar")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void reativar_professor_retorna204() throws Exception {
        mockMvc.perform(patch("/api/projetos/1/reativar")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isNoContent());
    }

    // --- POST /api/projetos/{id}/participantes ---

    @Test
    void adicionarParticipante_coordenador_retorna200() throws Exception {
        mockMvc.perform(post("/api/projetos/1/participantes")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3}"))
                .andExpect(status().isOk());
    }

    @Test
    void adicionarParticipante_duplicado_retorna409() throws Exception {
        when(projetoService.adicionarParticipante(eq(1L), eq(3L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(409),
                        "O usuário já é participante deste projeto."));
        mockMvc.perform(post("/api/projetos/1/participantes")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void adicionarParticipante_projetoInativo_retorna422() throws Exception {
        when(projetoService.adicionarParticipante(eq(1L), eq(3L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Não é possível adicionar participantes a um projeto inativo."));
        mockMvc.perform(post("/api/projetos/1/participantes")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // --- DELETE /api/projetos/{id}/participantes/{usuarioId} ---

    @Test
    void removerParticipante_professor_retorna200() throws Exception {
        mockMvc.perform(delete("/api/projetos/1/participantes/3")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void removerParticipante_usuario_retorna403() throws Exception {
        mockMvc.perform(delete("/api/projetos/1/participantes/3")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    // --- paginação e serialização ---

    @Test
    void listar_paginacaoNoResponse() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").exists());
    }

    @Test
    void listar_formatoJsonPaginaResponse() throws Exception {
        mockMvc.perform(get("/api/projetos")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").isNumber())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }
}
