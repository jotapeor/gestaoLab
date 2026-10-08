package com.main.gestaolabback;

import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.LaboratorioRepository;
import com.main.gestaolabback.repository.ReservaLaboratorioRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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

    private Usuario criarUsuario(String email, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Usuário Teste");
        u.setEmail(email);
        u.setSenha("$2a$10$hashedpassword");
        u.setPerfil(perfil);
        u.setAtivo(true);
        return usuarioRepo.save(u);
    }
}
