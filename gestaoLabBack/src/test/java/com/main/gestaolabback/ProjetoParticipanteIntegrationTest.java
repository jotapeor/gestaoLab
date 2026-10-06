package com.main.gestaolabback;

import com.main.gestaolabback.dto.ProjetoResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.helper.TestDataFactory;
import com.main.gestaolabback.model.*;
import com.main.gestaolabback.repository.ProjetoRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.ProjetoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjetoService.class)
class ProjetoParticipanteIntegrationTest {

    @Autowired private TestEntityManager em;
    @Autowired private ProjetoRepository projetoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProjetoService service;

    @Test
    void adicionarParticipante_persisteNaTabelaUsuarioProjeto() {
        Usuario professor = criarEsalvarProfessor("prof@test.com");
        Usuario aluno = criarEsalvarAluno("aluno@test.com");
        Projeto projeto = criarEsalvarProjeto("TCC", professor);

        service.adicionarParticipante(projeto.getId(), aluno.getId(), autProfessor(professor));
        em.flush();
        em.clear();

        Usuario alunoRelido = usuarioRepository.findById(aluno.getId()).orElseThrow();
        assertThat(alunoRelido.getProjetos()).hasSize(1);
        assertThat(alunoRelido.getProjetos().get(0).getId()).isEqualTo(projeto.getId());

        Projeto projetoRelido = projetoRepository.findByIdWithParticipantes(projeto.getId()).orElseThrow();
        assertThat(projetoRelido.getUsuarios()).hasSize(1);
        assertThat(projetoRelido.getUsuarios().get(0).getId()).isEqualTo(aluno.getId());
    }

    @Test
    void adicionarParticipante_alunoVeProjetoEmMeusProjetos() {
        Usuario professor = criarEsalvarProfessor("prof2@test.com");
        Usuario aluno = criarEsalvarAluno("aluno2@test.com");
        Projeto projeto = criarEsalvarProjeto("IC", professor);

        service.adicionarParticipante(projeto.getId(), aluno.getId(), autProfessor(professor));
        em.flush();
        em.clear();

        UsuarioAutenticado autAluno = new UsuarioAutenticado(aluno.getId(), "aluno2@test.com", "Aluno2", "USUARIO", false);
        List<ProjetoResponse> meus = service.meusProjetos(autAluno);
        assertThat(meus).hasSize(1);
        assertThat(meus.get(0).id()).isEqualTo(projeto.getId());
    }

    @Test
    void removerParticipante_removeDaTabelaUsuarioProjeto() {
        Usuario professor = criarEsalvarProfessor("prof3@test.com");
        Usuario aluno = criarEsalvarAluno("aluno3@test.com");
        Projeto projeto = criarEsalvarProjeto("TCC II", professor);

        aluno.adicionarProjeto(projeto);
        usuarioRepository.save(aluno);
        em.flush();
        em.clear();

        service.removerParticipante(projeto.getId(), aluno.getId(), autProfessor(professor));
        em.flush();
        em.clear();

        Usuario alunoRelido = usuarioRepository.findById(aluno.getId()).orElseThrow();
        assertThat(alunoRelido.getProjetos()).isEmpty();

        Projeto projetoRelido = projetoRepository.findByIdWithParticipantes(projeto.getId()).orElseThrow();
        assertThat(projetoRelido.getUsuarios()).isEmpty();
    }

    private Usuario criarEsalvarProfessor(String email) {
        Usuario u = TestDataFactory.criarUsuario("Professor Teste", email);
        u.setPerfil(PerfilUsuario.PROFESSOR);
        return usuarioRepository.save(u);
    }

    private Usuario criarEsalvarAluno(String email) {
        Usuario u = TestDataFactory.criarUsuario("Aluno Teste", email);
        u.setPerfil(PerfilUsuario.USUARIO);
        return usuarioRepository.save(u);
    }

    private Projeto criarEsalvarProjeto(String titulo, Usuario orientador) {
        Projeto p = TestDataFactory.criarProjeto(titulo, TipoProjeto.TCC_I);
        p.setOrientador(orientador);
        return projetoRepository.save(p);
    }

    private static UsuarioAutenticado autProfessor(Usuario professor) {
        return new UsuarioAutenticado(professor.getId(), professor.getEmail(), professor.getNome(), "PROFESSOR", false);
    }
}
