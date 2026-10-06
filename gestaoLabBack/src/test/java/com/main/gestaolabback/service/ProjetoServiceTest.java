package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.ProjetoDetalheResponse;
import com.main.gestaolabback.dto.ProjetoRequest;
import com.main.gestaolabback.dto.ProjetoResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Projeto;
import com.main.gestaolabback.model.TipoProjeto;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.ProjetoRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock ProjetoRepository projetoRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks ProjetoService service;

    private Usuario coordenador;
    private Usuario professor;
    private Usuario aluno;
    private Projeto projeto;
    private UsuarioAutenticado autCoordenador;
    private UsuarioAutenticado autProfessor;
    private UsuarioAutenticado autUsuario;

    @BeforeEach
    void setUp() {
        coordenador = usuario(1L, "Coord", PerfilUsuario.COORDENADOR, true);
        professor = usuario(2L, "Prof", PerfilUsuario.PROFESSOR, true);
        aluno = usuario(3L, "Aluno", PerfilUsuario.USUARIO, true);
        projeto = projeto(10L, "Projeto A", professor, true, new ArrayList<>());
        autCoordenador = new UsuarioAutenticado(1L, "coord@lab.com", "Coord", "COORDENADOR", false);
        autProfessor = new UsuarioAutenticado(2L, "prof@lab.com", "Prof", "PROFESSOR", false);
        autUsuario = new UsuarioAutenticado(3L, "aluno@lab.com", "Aluno", "USUARIO", false);
    }

    // ---------- criar ----------

    @Test
    void criar_coordenador_usaOrientadorDoRequest() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(projetoRepository.save(any())).thenAnswer(i -> { Projeto p = i.getArgument(0); p.setId(10L); return p; });
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, 2L, null);
        ProjetoResponse resp = service.criar(req, autCoordenador);
        assertThat(resp.orientador().id()).isEqualTo(2L);
    }

    @Test
    void criar_professor_ignoraOrientadorDoRequestEUsaProprioId() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(projetoRepository.save(any())).thenAnswer(i -> { Projeto p = i.getArgument(0); p.setId(10L); return p; });
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, 99L, null);
        ProjetoResponse resp = service.criar(req, autProfessor);
        assertThat(resp.orientador().id()).isEqualTo(2L);
    }

    @Test
    void criar_semOrientadorNaRequest_coordenador_lanca422() {
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, null, null);
        assertThatThrownBy(() -> service.criar(req, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_orientadorInativo_lanca422() {
        professor.setAtivo(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, 2L, null);
        assertThatThrownBy(() -> service.criar(req, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_orientadorPerfilUsuario_lanca422() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        ProjetoRequest req = new ProjetoRequest("TCC", TipoProjeto.TCC_I, 3L, null);
        assertThatThrownBy(() -> service.criar(req, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    // ---------- atualizar ----------

    @Test
    void atualizar_professor_naoPodeEditarProjetoDeOutro() {
        Projeto outroProj = projeto(11L, "Outro", coordenador, true, new ArrayList<>());
        when(projetoRepository.findById(11L)).thenReturn(Optional.of(outroProj));
        ProjetoRequest req = new ProjetoRequest("Novo", TipoProjeto.TCC_I, null, null);
        assertThatThrownBy(() -> service.atualizar(11L, req, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    @Test
    void atualizar_professor_podeEditarSeuProjeto() {
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(projetoRepository.save(any())).thenReturn(projeto);
        when(projetoRepository.countParticipantes(10L)).thenReturn(0L);
        ProjetoRequest req = new ProjetoRequest("Novo titulo", TipoProjeto.TCC_I, null, null);
        ProjetoResponse resp = service.atualizar(10L, req, autProfessor);
        assertThat(resp.titulo()).isEqualTo("Novo titulo");
    }

    // ---------- inativar / reativar ----------

    @Test
    void inativar_professor_seuProjeto_setAtivoFalse() {
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        when(projetoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.inativar(10L, autProfessor);
        verify(projetoRepository).save(argThat(p -> !p.isAtivo()));
    }

    @Test
    void inativar_professor_projetoDeOutro_lanca403() {
        Projeto outroProj = projeto(11L, "Outro", coordenador, true, new ArrayList<>());
        when(projetoRepository.findById(11L)).thenReturn(Optional.of(outroProj));
        assertThatThrownBy(() -> service.inativar(11L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    // ---------- adicionarParticipante ----------

    @Test
    void adicionarParticipante_sucesso() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        Projeto projetoComParticipante = projeto(10L, "Projeto A", professor, true, new ArrayList<>(List.of(aluno)));
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto)).thenReturn(Optional.of(projetoComParticipante));
        ProjetoDetalheResponse resp = service.adicionarParticipante(10L, 3L, autProfessor);
        assertThat(resp.participantes()).hasSize(1);
    }

    @Test
    void adicionarParticipante_duplicado_lanca409() {
        projeto.getUsuarios().add(aluno);
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        assertThatThrownBy(() -> service.adicionarParticipante(10L, 3L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(409));
    }

    @Test
    void adicionarParticipante_emProjetoInativo_lanca422() {
        Projeto inativo = projeto(10L, "Proj", professor, false, new ArrayList<>());
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(inativo));
        assertThatThrownBy(() -> service.adicionarParticipante(10L, 3L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void adicionarParticipante_perfilProfessor_lanca422() {
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
        assertThatThrownBy(() -> service.adicionarParticipante(10L, 2L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void adicionarParticipante_professorNaoOrientador_lanca403() {
        Projeto outroProj = projeto(11L, "Outro", coordenador, true, new ArrayList<>());
        when(projetoRepository.findByIdWithParticipantes(11L)).thenReturn(Optional.of(outroProj));
        assertThatThrownBy(() -> service.adicionarParticipante(11L, 3L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    // ---------- removerParticipante ----------

    @Test
    void removerParticipante_sucesso() {
        projeto.getUsuarios().add(aluno);
        Projeto semParticipante = projeto(10L, "Projeto A", professor, true, new ArrayList<>());
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto)).thenReturn(Optional.of(semParticipante));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aluno));
        ProjetoDetalheResponse resp = service.removerParticipante(10L, 3L, autProfessor);
        assertThat(resp.participantes()).isEmpty();
    }

    @Test
    void removerParticipante_emProjetoInativo_lanca422() {
        Projeto inativo = projeto(10L, "Proj", professor, false, new ArrayList<>(List.of(aluno)));
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(inativo));
        assertThatThrownBy(() -> service.removerParticipante(10L, 3L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_usuario_participante_retorna() {
        projeto.getUsuarios().add(aluno);
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto));
        ProjetoDetalheResponse resp = service.buscarPorId(10L, autUsuario);
        assertThat(resp.id()).isEqualTo(10L);
    }

    @Test
    void buscarPorId_usuario_naoParticipante_lanca403() {
        when(projetoRepository.findByIdWithParticipantes(10L)).thenReturn(Optional.of(projeto));
        assertThatThrownBy(() -> service.buscarPorId(10L, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    @Test
    void buscarPorId_professor_naoOrientador_lanca403() {
        Projeto outroProj = projeto(11L, "Outro", coordenador, true, new ArrayList<>());
        when(projetoRepository.findByIdWithParticipantes(11L)).thenReturn(Optional.of(outroProj));
        assertThatThrownBy(() -> service.buscarPorId(11L, autProfessor))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    @Test
    void buscarPorId_inexistente_lanca404() {
        when(projetoRepository.findByIdWithParticipantes(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L, autCoordenador))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(404));
    }

    // ---------- meusProjetos ----------

    @Test
    void meusProjetos_usuario_retornaProjetosDoParticipante() {
        when(projetoRepository.findActiveByParticipanteId(3L)).thenReturn(List.of(projeto));
        when(projetoRepository.countParticipantes(10L)).thenReturn(1L);
        List<ProjetoResponse> lista = service.meusProjetos(autUsuario);
        assertThat(lista).hasSize(1);
    }

    @Test
    void meusProjetos_professor_retornaProjetosOrientados() {
        when(projetoRepository.findMeusByOrientadorId(2L)).thenReturn(List.of(projeto));
        when(projetoRepository.countParticipantes(10L)).thenReturn(0L);
        List<ProjetoResponse> lista = service.meusProjetos(autProfessor);
        assertThat(lista).hasSize(1);
    }

    // ---------- helpers ----------

    private static Usuario usuario(Long id, String nome, PerfilUsuario perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setPerfil(perfil);
        u.setAtivo(ativo);
        return u;
    }

    private static Projeto projeto(Long id, String titulo, Usuario orientador, boolean ativo, List<Usuario> usuarios) {
        Projeto p = new Projeto();
        p.setId(id);
        p.setTitulo(titulo);
        p.setOrientador(orientador);
        p.setAtivo(ativo);
        p.setUsuarios(usuarios);
        return p;
    }

    private static int status(Throwable e) {
        return ((ResponseStatusException) e).getStatusCode().value();
    }
}
