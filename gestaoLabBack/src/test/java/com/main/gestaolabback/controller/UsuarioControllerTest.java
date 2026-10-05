package com.main.gestaolabback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.gestaolabback.dto.MeuPerfilRequest;
import com.main.gestaolabback.dto.ResponsavelResponse;
import com.main.gestaolabback.dto.UsuarioRequest;
import com.main.gestaolabback.dto.UsuarioResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.TokenService;
import com.main.gestaolabback.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
class UsuarioControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private UsuarioService usuarioService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenCoordenador;
    private String tokenProfessor;
    private String tokenUsuario;

    private UsuarioResponse usuarioResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenCoordenador = gerarToken(PerfilUsuario.COORDENADOR);
        tokenProfessor = gerarToken(PerfilUsuario.PROFESSOR);
        tokenUsuario = gerarToken(PerfilUsuario.USUARIO);

        usuarioResponse = new UsuarioResponse(1L, "Ana", null, "ana@lab.com",
                PerfilUsuario.USUARIO, true, true, null, null, null);
        Page<UsuarioResponse> page = new PageImpl<>(List.of(usuarioResponse));

        when(usuarioService.listar(any(), any(), any(), any(), any(), anyInt())).thenReturn(page);
        when(usuarioService.buscarPorId(anyLong(), any())).thenReturn(usuarioResponse);
        when(usuarioService.criar(any())).thenReturn(usuarioResponse);
        when(usuarioService.atualizar(anyLong(), any())).thenReturn(usuarioResponse);
        doNothing().when(usuarioService).inativar(anyLong(), any());
        doNothing().when(usuarioService).reativar(anyLong());
        doNothing().when(usuarioService).redefinirSenha(anyLong(), any());
        when(usuarioService.listarResponsaveis()).thenReturn(List.of(new ResponsavelResponse(1L, "Prof")));
        when(usuarioService.me(anyLong())).thenReturn(usuarioResponse);
        when(usuarioService.atualizarMe(anyLong(), any())).thenReturn(usuarioResponse);
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

    // --- GET /api/usuarios ---

    @Test
    void listar_coordenador_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk());
    }

    @Test
    void listar_professor_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void listar_usuario_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_comFiltros_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .param("busca", "Ana")
                        .param("perfil", "USUARIO")
                        .param("ativo", "true")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk());
    }

    // --- GET /api/usuarios/{id} ---

    @Test
    void buscarPorId_coordenador_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@lab.com"));
    }

    @Test
    void buscarPorId_professor_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_usuario_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(usuarioService.buscarPorId(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));
        mockMvc.perform(get("/api/usuarios/99")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado."));
    }

    @Test
    void buscarPorId_professor_semPermissao_retorna403() throws Exception {
        when(usuarioService.buscarPorId(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado."));
        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/usuarios ---

    @Test
    void criar_coordenador_retorna201() throws Exception {
        UsuarioRequest req = new UsuarioRequest("Ana", null, "ana@lab.com",
                PerfilUsuario.USUARIO, null, null, "senha123");
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@lab.com"));
    }

    @Test
    void criar_professor_retorna403() throws Exception {
        UsuarioRequest req = new UsuarioRequest("Ana", null, "ana@lab.com",
                PerfilUsuario.USUARIO, null, null, "senha123");
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"invalido\",\"senhaProvisoria\":\"senha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").exists());
    }

    @Test
    void criar_emailDuplicado_retorna409() throws Exception {
        when(usuarioService.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(409),
                        "Já existe um usuário com o e-mail \"ana@lab.com\"."));
        UsuarioRequest req = new UsuarioRequest("Ana", null, "ana@lab.com",
                PerfilUsuario.USUARIO, null, null, "senha123");
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void criar_nomeVazio_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"ana@lab.com\",\"senhaProvisoria\":\"senha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    // --- PATCH inativar/reativar ---

    @Test
    void inativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void inativar_professor_retorna403() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/inativar")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isForbidden());
    }

    @Test
    void inativar_propriaConta_retorna422() throws Exception {
        doThrow(new ResponseStatusException(HttpStatusCode.valueOf(422),
                "Você não pode inativar sua própria conta."))
                .when(usuarioService).inativar(anyLong(), any());
        mockMvc.perform(patch("/api/usuarios/1/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Você não pode inativar sua própria conta."));
    }

    @Test
    void inativar_ultimoCoordenador_retorna422() throws Exception {
        doThrow(new ResponseStatusException(HttpStatusCode.valueOf(422),
                "O sistema precisa ter pelo menos um coordenador ativo."))
                .when(usuarioService).inativar(anyLong(), any());
        mockMvc.perform(patch("/api/usuarios/2/inativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void reativar_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/reativar")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isNoContent());
    }

    @Test
    void reativar_usuario_retorna403() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/reativar")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    // --- PATCH redefinir-senha ---

    @Test
    void redefinirSenha_coordenador_retorna204() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/redefinir-senha")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaSenhaProvisoria\":\"novaSenha123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void redefinirSenha_professor_retorna403() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/redefinir-senha")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaSenhaProvisoria\":\"novaSenha123\"}"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/usuarios/responsaveis ---

    @Test
    void listarResponsaveis_coordenador_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios/responsaveis")
                        .header("Authorization", "Bearer " + tokenCoordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Prof"));
    }

    @Test
    void listarResponsaveis_professor_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios/responsaveis")
                        .header("Authorization", "Bearer " + tokenProfessor))
                .andExpect(status().isForbidden());
    }

    // --- GET/PUT /api/usuarios/me ---

    @Test
    void me_qualquerPerfil_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarMe_nomeVazio_retorna400() throws Exception {
        mockMvc.perform(put("/api/usuarios/me")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void atualizarMe_coordenador_retorna200() throws Exception {
        mockMvc.perform(put("/api/usuarios/me")
                        .header("Authorization", "Bearer " + tokenCoordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MeuPerfilRequest("Novo Nome"))))
                .andExpect(status().isOk());
    }

    @Test
    void me_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }
}
