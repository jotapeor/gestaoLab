package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.MeuPerfilRequest;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.dto.UsuarioRequest;
import com.main.gestaolabback.model.CursoSetor;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.CursoSetorRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock CursoSetorRepository cursoSetorRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UsuarioService service;

    private Usuario coordenador;
    private Usuario professor;
    private Usuario aluno;
    private UsuarioAutenticado autCoordenador;

    @BeforeEach
    void setUp() {
        coordenador = usuario(1L, "Coord", "coord@lab.com", PerfilUsuario.COORDENADOR, true);
        professor = usuario(2L, "Prof", "prof@lab.com", PerfilUsuario.PROFESSOR, true);
        aluno = usuario(3L, "Aluno", "aluno@lab.com", PerfilUsuario.USUARIO, true);
        autCoordenador = new UsuarioAutenticado(1L, "coord@lab.com", "Coord", "COORDENADOR", false);
    }

    // ---------- criar ----------

    @Test
    void criar_semSenha_lanca400() {
        UsuarioRequest req = req(null, "Ana", "ana@lab.com", null, null, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(400));
    }

    @Test
    void criar_senhaInferior8Chars_lanca400() {
        UsuarioRequest req = req("abc1234", "Ana", "ana@lab.com", null, null, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(400));
    }

    @Test
    void criar_emailDuplicado_lanca409() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@lab.com")).thenReturn(true);
        UsuarioRequest req = req("senha123", "Ana", "ana@lab.com", null, null, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(409));
    }

    @Test
    void criar_matriculaDuplicada_lanca409() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.existsByMatricula("MAT001")).thenReturn(true);
        UsuarioRequest req = req("senha123", "Ana", "ana@lab.com", null, null, "MAT001", null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(409));
    }

    @Test
    void criar_cursoSetorInativo_lanca422() {
        CursoSetor cs = new CursoSetor();
        cs.setAtivo(false);
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(cursoSetorRepository.findById(5L)).thenReturn(Optional.of(cs));
        UsuarioRequest req = req("senha123", "Ana", "ana@lab.com", 5L, null, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_responsavelInexistente_lanca404() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        UsuarioRequest req = req("senha123", "Ana", "ana@lab.com", null, 99L, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(404));
    }

    @Test
    void criar_responsavelPerfilUsuario_lanca422() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        UsuarioRequest req = req("senha123", "Ana", "ana@lab.com", null, 3L, null, null);
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_sucesso_setPrimeiroAcessoTrue() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        Usuario salvo = usuario(10L, "Ana", "ana@lab.com", PerfilUsuario.USUARIO, true);
        salvo.setPrimeiroAcesso(true);
        when(usuarioRepository.save(any())).thenReturn(salvo);
        service.criar(req("senha123", "Ana", "ana@lab.com", null, null, null, null));
        verify(passwordEncoder).encode("senha123");
    }

    // ---------- atualizar ----------

    @Test
    void atualizar_emailDuplicadoDeOutro_lanca409() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("outro@lab.com", 3L)).thenReturn(true);
        UsuarioRequest req = req(null, "Aluno", "outro@lab.com", null, null, null, null);
        assertThatThrownBy(() -> service.atualizar(3L, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(409));
    }

    @Test
    void atualizar_responsavelProprioUsuario_lanca422() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(3L))).thenReturn(false);
        UsuarioRequest req = req(null, "Aluno", "aluno@lab.com", null, 3L, null, null);
        assertThatThrownBy(() -> service.atualizar(3L, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    // ---------- inativar ----------

    @Test
    void inativar_propriaConta_lanca422() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(coordenador));
        assertThatThrownBy(() -> service.inativar(1L, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void inativar_ultimoCoordenador_lanca422() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(
                usuario(2L, "Coord2", "coord2@lab.com", PerfilUsuario.COORDENADOR, true)));
        when(usuarioRepository.countByPerfilAndAtivo(PerfilUsuario.COORDENADOR, true)).thenReturn(1L);
        assertThatThrownBy(() -> service.inativar(2L, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void inativar_comOutroCoordenador_setsAtivoFalse() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(
                usuario(2L, "Coord2", "coord2@lab.com", PerfilUsuario.COORDENADOR, true)));
        when(usuarioRepository.countByPerfilAndAtivo(PerfilUsuario.COORDENADOR, true)).thenReturn(2L);
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.inativar(2L, autCoordenador);
        verify(usuarioRepository).save(argThat(u -> !u.isAtivo()));
    }

    @Test
    void inativar_aluno_setsAtivoFalse() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.inativar(3L, autCoordenador);
        verify(usuarioRepository).save(argThat(u -> !u.isAtivo()));
    }

    @Test
    void inativar_inexistente_lanca404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.inativar(99L, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(404));
    }

    // ---------- reativar ----------

    @Test
    void reativar_setsAtivoTrue() {
        Usuario inativo = usuario(3L, "Aluno", "aluno@lab.com", PerfilUsuario.USUARIO, false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(inativo));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.reativar(3L);
        verify(usuarioRepository).save(argThat(Usuario::isAtivo));
    }

    // ---------- redefinirSenha ----------

    @Test
    void redefinirSenha_senhaInferior8Chars_lanca400() {
        assertThatThrownBy(() -> service.redefinirSenha(1L, "abc"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(400));
    }

    @Test
    void redefinirSenha_sucesso_setPrimeiroAcessoTrue() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.redefinirSenha(3L, "novaSenha123");
        verify(usuarioRepository).save(argThat(u -> u.isPrimeiroAcesso() && "hashed".equals(u.getSenha())));
    }

    // ---------- listar ----------

    @Test
    void listar_coordenador_usaFiltroGeral() {
        when(usuarioRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(aluno)));
        Page<com.main.gestaolabback.dto.UsuarioResponse> result = service.listar(autCoordenador, null, null, null, null, 0);
        assertThat(result.getContent()).hasSize(1);
        verify(usuarioRepository).findWithFilters(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void listar_professor_usaFiltroResponsavel() {
        UsuarioAutenticado autProf = new UsuarioAutenticado(2L, "prof@lab.com", "Prof", "PROFESSOR", false);
        when(usuarioRepository.findByResponsavelWithFilters(eq(2L), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(aluno)));
        service.listar(autProf, null, null, null, null, 0);
        verify(usuarioRepository).findByResponsavelWithFilters(eq(2L), any(), any(), any(), any(), any(Pageable.class));
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_professor_semResponsavel_lanca403() {
        UsuarioAutenticado autProf = new UsuarioAutenticado(2L, "prof@lab.com", "Prof", "PROFESSOR", false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        assertThatThrownBy(() -> service.buscarPorId(3L, autProf))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    @Test
    void buscarPorId_professor_responsavelCorreto_retornaResponse() {
        UsuarioAutenticado autProf = new UsuarioAutenticado(2L, "prof@lab.com", "Prof", "PROFESSOR", false);
        aluno.setResponsavel(professor);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        com.main.gestaolabback.dto.UsuarioResponse resp = service.buscarPorId(3L, autProf);
        assertThat(resp.id()).isEqualTo(3L);
    }

    // ---------- me ----------

    @Test
    void atualizarMe_atualizaNome() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(coordenador));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        com.main.gestaolabback.dto.UsuarioResponse resp = service.atualizarMe(1L, new MeuPerfilRequest("Novo Nome"));
        assertThat(resp.nome()).isEqualTo("Novo Nome");
    }

    // ---------- helpers ----------

    private static Usuario usuario(Long id, String nome, String email, PerfilUsuario perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setPerfil(perfil);
        u.setAtivo(ativo);
        u.setSenha("hashed");
        return u;
    }

    private static UsuarioRequest req(String senha, String nome, String email,
                                       Long cursoSetorId, Long responsavelId,
                                       String matricula, PerfilUsuario perfil) {
        return new UsuarioRequest(nome, matricula, email, perfil, cursoSetorId, responsavelId, senha);
    }

    private static int status(Throwable e) {
        return ((ResponseStatusException) e).getStatusCode().value();
    }
}
