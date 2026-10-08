package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.service.AuthApiService;
import com.main.gestaolabfront.service.CursoSetorApiService;
import com.main.gestaolabfront.service.LaboratorioApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthApiService authApiService;

    @MockitoBean
    private UsuarioApiService usuarioApiService;

    @MockitoBean
    private CursoSetorApiService cursoSetorApiService;

    @MockitoBean
    private LaboratorioApiService laboratorioApiService;

    @Test
    void paginaInexistente_retornaNotFound() throws Exception {
        mockMvc.perform(get("/caminho-inexistente-abc123")
                        .sessionAttr("token", "tok")
                        .sessionAttr("perfil", "COORDENADOR")
                        .sessionAttr("primeiroAcesso", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    void favicon_acessivelSemSessao() throws Exception {
        mockMvc.perform(get("/img/favicon.png"))
                .andExpect(status().isOk());
    }
}
