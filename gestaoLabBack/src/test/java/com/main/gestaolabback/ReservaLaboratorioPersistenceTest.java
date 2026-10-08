package com.main.gestaolabback;

import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Projeto;
import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.model.TipoProjeto;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.LaboratorioRepository;
import com.main.gestaolabback.repository.ProjetoRepository;
import com.main.gestaolabback.repository.ReservaLaboratorioRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservaLaboratorioPersistenceTest {

    @Autowired private TestEntityManager em;
    @Autowired private ReservaLaboratorioRepository reservaRepo;
    @Autowired private LaboratorioRepository laboratorioRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ProjetoRepository projetoRepo;

    @Test
    void colunasCancelamento_persistidasERelidas() {
        Laboratorio lab = new Laboratorio();
        lab.setNome("Lab Cancelamento");
        lab.setAtivo(true);
        lab = laboratorioRepo.save(lab);

        Usuario aluno = criarUsuario("aluno.cancel@test.com", PerfilUsuario.USUARIO);
        Usuario coord = criarUsuario("coord.cancel@test.com", PerfilUsuario.COORDENADOR);

        LocalDateTime inicio = LocalDateTime.of(2026, 11, 5, 10, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 11, 5, 12, 0);

        ReservaLaboratorio reserva = new ReservaLaboratorio();
        reserva.setLaboratorio(lab);
        reserva.setUsuario(aluno);
        reserva.setDataInicio(inicio);
        reserva.setDataFim(fim);
        reserva.setMotivo("Experimento de persistência");
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setDataCriacao(LocalDateTime.now());
        reserva = reservaRepo.save(reserva);

        LocalDateTime dataCancelamento = LocalDateTime.of(2026, 11, 4, 15, 30);
        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setCanceladoPor(coord);
        reserva.setDataCancelamento(dataCancelamento);
        reserva.setMotivoCancelamento("Laboratório em manutenção");
        reservaRepo.save(reserva);

        em.flush();
        em.clear();

        Optional<ReservaLaboratorio> relida = reservaRepo.findByIdWithDetails(reserva.getId());
        assertThat(relida).isPresent();

        ReservaLaboratorio r = relida.get();
        assertThat(r.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(r.getCanceladoPor()).isNotNull();
        assertThat(r.getCanceladoPor().getId()).isEqualTo(coord.getId());
        assertThat(r.getDataCancelamento()).isEqualTo(dataCancelamento);
        assertThat(r.getMotivoCancelamento()).isEqualTo("Laboratório em manutenção");
    }

    @Test
    void semCancelamento_colunasNulas() {
        Laboratorio lab = new Laboratorio();
        lab.setNome("Lab Nulo");
        lab.setAtivo(true);
        lab = laboratorioRepo.save(lab);

        Usuario aluno = criarUsuario("aluno.nulo@test.com", PerfilUsuario.USUARIO);

        ReservaLaboratorio reserva = new ReservaLaboratorio();
        reserva.setLaboratorio(lab);
        reserva.setUsuario(aluno);
        reserva.setDataInicio(LocalDateTime.of(2026, 11, 6, 10, 0));
        reserva.setDataFim(LocalDateTime.of(2026, 11, 6, 11, 0));
        reserva.setMotivo("Teste nulo");
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setDataCriacao(LocalDateTime.now());
        reserva = reservaRepo.save(reserva);

        em.flush();
        em.clear();

        Optional<ReservaLaboratorio> relida = reservaRepo.findByIdWithDetails(reserva.getId());
        assertThat(relida).isPresent();
        assertThat(relida.get().getCanceladoPor()).isNull();
        assertThat(relida.get().getDataCancelamento()).isNull();
        assertThat(relida.get().getMotivoCancelamento()).isNull();
    }

    // ---------- motivo de cancelamento ----------

    @Test
    void motivoCancelamento_persistidoERelido() {
        Laboratorio lab = criarLaboratorio("Lab Motivo");
        Usuario aluno = criarUsuario("aluno.motivo@test.com", PerfilUsuario.USUARIO);
        Usuario coord = criarUsuario("coord.motivo@test.com", PerfilUsuario.COORDENADOR);

        ReservaLaboratorio reserva = criarReserva(lab, aluno, null,
                LocalDateTime.of(2026, 11, 20, 8, 0), LocalDateTime.of(2026, 11, 20, 10, 0));

        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setCanceladoPor(coord);
        reserva.setDataCancelamento(LocalDateTime.of(2026, 11, 19, 14, 0));
        reserva.setMotivoCancelamento("Equipamento em manutenção");
        reservaRepo.save(reserva);

        em.flush();
        em.clear();

        Optional<ReservaLaboratorio> relida = reservaRepo.findByIdWithDetails(reserva.getId());
        assertThat(relida).isPresent();
        assertThat(relida.get().getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(relida.get().getMotivoCancelamento()).isEqualTo("Equipamento em manutenção");
        assertThat(relida.get().getCanceladoPor().getId()).isEqualTo(coord.getId());
    }

    // ---------- disponibilidade: canceladas não contam ----------

    @Test
    void findConfirmadasSobrepostas_reservaCancelada_naoRetorna() {
        Laboratorio lab = criarLaboratorio("Lab Cancelada Disp");
        Usuario aluno = criarUsuario("aluno.disp.cancel@test.com", PerfilUsuario.USUARIO);

        ReservaLaboratorio cancelada = criarReserva(lab, aluno, null,
                LocalDateTime.of(2026, 12, 1, 10, 0), LocalDateTime.of(2026, 12, 1, 12, 0));
        cancelada.setStatus(StatusReserva.CANCELADA);
        reservaRepo.save(cancelada);

        em.flush();
        em.clear();

        List<ReservaLaboratorio> sobrepostas = reservaRepo.findConfirmadasSobrepostas(
                lab.getId(),
                LocalDateTime.of(2026, 12, 1, 10, 0),
                LocalDateTime.of(2026, 12, 1, 12, 0));
        assertThat(sobrepostas).isEmpty();
    }

    // ---------- professor: visibilidade na listagem ----------

    @Test
    void listar_professor_veReservaDoOrientando() {
        Usuario professor = criarUsuario("prof.1@test.com", PerfilUsuario.PROFESSOR);
        Usuario aluno    = criarUsuario("aluno.1@test.com", PerfilUsuario.USUARIO);
        Laboratorio lab  = criarLaboratorio("Lab Prof 1");

        Projeto projeto = new Projeto();
        projeto.setTitulo("Projeto do Prof 1");
        projeto.setTipo(TipoProjeto.PESQUISA);
        projeto.setOrientador(professor);
        projeto = projetoRepo.save(projeto);

        ReservaLaboratorio reserva = criarReserva(lab, aluno, projeto,
                LocalDateTime.of(2026, 11, 10, 8, 0), LocalDateTime.of(2026, 11, 10, 10, 0));

        em.flush();
        em.clear();

        Page<ReservaLaboratorio> resultado = reservaRepo.findWithFiltersParaProfessor(
                professor.getId(), null, null, null, null, PageRequest.of(0, 20));

        assertThat(resultado.getContent())
                .extracting(ReservaLaboratorio::getId)
                .containsExactly(reserva.getId());
    }

    @Test
    void listar_professor_naoVeReservaDeProjetoDeOutroProfessor() {
        Usuario professor1 = criarUsuario("prof.a@test.com", PerfilUsuario.PROFESSOR);
        Usuario professor2 = criarUsuario("prof.b@test.com", PerfilUsuario.PROFESSOR);
        Usuario aluno      = criarUsuario("aluno.b@test.com", PerfilUsuario.USUARIO);
        Laboratorio lab    = criarLaboratorio("Lab Prof B");

        Projeto projeto = new Projeto();
        projeto.setTitulo("Projeto do Prof 2");
        projeto.setTipo(TipoProjeto.PESQUISA);
        projeto.setOrientador(professor2);
        projeto = projetoRepo.save(projeto);

        criarReserva(lab, aluno, projeto,
                LocalDateTime.of(2026, 11, 11, 8, 0), LocalDateTime.of(2026, 11, 11, 10, 0));

        em.flush();
        em.clear();

        Page<ReservaLaboratorio> resultado = reservaRepo.findWithFiltersParaProfessor(
                professor1.getId(), null, null, null, null, PageRequest.of(0, 20));

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    void listar_professor_naoVeReservaSemProjetoDeOutroUsuario() {
        Usuario professor = criarUsuario("prof.c@test.com", PerfilUsuario.PROFESSOR);
        Usuario outroAluno = criarUsuario("aluno.c@test.com", PerfilUsuario.USUARIO);
        Laboratorio lab    = criarLaboratorio("Lab Prof C");

        criarReserva(lab, outroAluno, null,
                LocalDateTime.of(2026, 11, 12, 8, 0), LocalDateTime.of(2026, 11, 12, 10, 0));

        em.flush();
        em.clear();

        Page<ReservaLaboratorio> resultado = reservaRepo.findWithFiltersParaProfessor(
                professor.getId(), null, null, null, null, PageRequest.of(0, 20));

        assertThat(resultado.getContent()).isEmpty();
    }

    private Usuario criarUsuario(String email, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Usuário Teste");
        u.setEmail(email);
        u.setSenha("$2a$10$hashedpassword");
        u.setPerfil(perfil);
        u.setAtivo(true);
        return usuarioRepo.save(u);
    }

    private Laboratorio criarLaboratorio(String nome) {
        Laboratorio lab = new Laboratorio();
        lab.setNome(nome);
        lab.setAtivo(true);
        return laboratorioRepo.save(lab);
    }

    private ReservaLaboratorio criarReserva(Laboratorio lab, Usuario usuario, Projeto projeto,
                                             LocalDateTime inicio, LocalDateTime fim) {
        ReservaLaboratorio r = new ReservaLaboratorio();
        r.setLaboratorio(lab);
        r.setUsuario(usuario);
        r.setProjeto(projeto);
        r.setDataInicio(inicio);
        r.setDataFim(fim);
        r.setMotivo(projeto != null ? "Reserva do projeto" : "Reserva pessoal");
        r.setStatus(StatusReserva.CONFIRMADA);
        r.setDataCriacao(LocalDateTime.now());
        return reservaRepo.save(r);
    }
}
