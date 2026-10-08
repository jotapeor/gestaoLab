package com.main.gestaolabback.service;

import com.main.gestaolabback.config.AppConfig;
import com.main.gestaolabback.dto.CancelarReservaRequest;
import com.main.gestaolabback.dto.DisponibilidadeResponse;
import com.main.gestaolabback.dto.ReservaLaboratorioRequest;
import com.main.gestaolabback.dto.ReservaLaboratorioResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.LaboratorioRepository;
import com.main.gestaolabback.repository.ProjetoRepository;
import com.main.gestaolabback.repository.ReservaLaboratorioRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ReservaLaboratorioServiceTest {

    // Clock fixed at 2026-11-01T10:00:00Z
    private static final Instant AGORA_INSTANT = Instant.parse("2026-11-01T10:00:00Z");
    private static final Clock CLOCK_FIXO = Clock.fixed(AGORA_INSTANT, ZoneOffset.UTC);
    private static final LocalDateTime AGORA = LocalDateTime.ofInstant(AGORA_INSTANT, ZoneOffset.UTC);

    @Mock private ReservaLaboratorioRepository reservaRepo;
    @Mock private LaboratorioRepository laboratorioRepo;
    @Mock private ProjetoRepository projetoRepo;
    @Mock private UsuarioRepository usuarioRepo;

    private AppConfig appConfig;
    private ReservaLaboratorioService service;

    private Usuario usuarioComum;
    private Usuario coordenador;
    private Laboratorio lab;
    private UsuarioAutenticado autUsuario;
    private UsuarioAutenticado autCoordenador;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
        service = new ReservaLaboratorioService(reservaRepo, laboratorioRepo, projetoRepo, usuarioRepo,
                appConfig, CLOCK_FIXO);

        usuarioComum = usuario(10L, "Aluno", PerfilUsuario.USUARIO);
        coordenador = usuario(1L, "Coord", PerfilUsuario.COORDENADOR);

        lab = new Laboratorio();
        lab.setId(5L);
        lab.setNome("Lab A");
        lab.setAtivo(true);
        lab.setCapacidade(2);

        autUsuario = new UsuarioAutenticado(10L, "aluno@lab.com", "Aluno", "USUARIO", false);
        autCoordenador = new UsuarioAutenticado(1L, "coord@lab.com", "Coord", "COORDENADOR", false);
    }

    // ---------- criar ----------

    @Test
    void criar_sucesso() {
        LocalDateTime inicio = AGORA.plusHours(2);
        LocalDateTime fim = AGORA.plusHours(3);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo teste", null);

        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of());
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            ReservaLaboratorio r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        ReservaLaboratorioResponse resp = service.criar(req, autUsuario);
        assertThat(resp.status()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(resp.motivo()).isEqualTo("Motivo teste");
    }

    @Test
    void criar_labInexistente_lanca404() {
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(99L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(404));
    }

    @Test
    void criar_labInativo_lanca422() {
        lab.setAtivo(false);
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_dataInicioNoPassado_lanca422() {
        LocalDateTime inicio = AGORA.minusHours(1);
        LocalDateTime fim = AGORA.plusHours(1);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_duracaoMenorQueMinimo_lanca422() {
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = inicio.plusMinutes(5); // 5 min < 15 min minimo
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_duracaoMaiorQueMaximo_lanca422() {
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = inicio.plusHours(13); // 13h > 12h maximo
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_antecedenciaMaiorQueMaximo_lanca422() {
        LocalDateTime inicio = AGORA.plusDays(61); // 61 dias > 60 dias maximo
        LocalDateTime fim = inicio.plusHours(1);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_semProjetoSemMotivo_lanca422() {
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, null, null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_usuarioJaTemReservaSobrepostaNoMesmoHorario_lanca422() {
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of(new ReservaLaboratorio()));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_laboratorioLotado_lanca422() {
        lab.setCapacidade(1);
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        // 1 existing reservation overlapping => peak = 2 > capacity 1
        ReservaLaboratorio existente = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void criar_intervalosTocam_naoConflitam() {
        // Existing: 10:00-11:00, New: 11:00-12:00 → touching → no conflict
        lab.setCapacidade(1);
        LocalDateTime inicio = AGORA.plusHours(1); // 11:00
        LocalDateTime fim = AGORA.plusHours(2);    // 12:00
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        // existing ends at 11:00 (AGORA + 1h) which equals new inicio => touching, query returns empty
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of());
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            ReservaLaboratorio r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        ReservaLaboratorioResponse resp = service.criar(req, autUsuario);
        assertThat(resp.status()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    void criar_sobreposicaoParcial_conflita() {
        lab.setCapacidade(1);
        LocalDateTime inicio = AGORA.plusMinutes(90); // 11:30
        LocalDateTime fim = AGORA.plusMinutes(150);   // 12:30
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        // existing: 11:00-12:00 overlaps with 11:30-12:30
        ReservaLaboratorio existente = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    // ---------- cancelar ----------

    @Test
    void cancelar_donoPodeAntesDaInicio() {
        LocalDateTime inicio = AGORA.plusHours(2);
        LocalDateTime fim = AGORA.plusHours(3);
        ReservaLaboratorio reserva = reservaConfirmada(10L, inicio, fim);
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(reservaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaLaboratorioResponse resp = service.cancelar(1L, new CancelarReservaRequest("Não vou usar"), autUsuario);
        assertThat(resp.status()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(resp.motivoCancelamento()).isEqualTo("Não vou usar");
    }

    @Test
    void cancelar_donoNaoPodeDepoisInicio_lanca422() {
        LocalDateTime inicio = AGORA.minusHours(1); // already started
        LocalDateTime fim = AGORA.plusHours(1);
        ReservaLaboratorio reserva = reservaConfirmada(10L, inicio, fim);
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(1L, null, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void cancelar_coordenadorPodeSempreAntesFim() {
        LocalDateTime inicio = AGORA.minusHours(1); // already started
        LocalDateTime fim = AGORA.plusHours(1);
        ReservaLaboratorio reserva = reservaConfirmada(10L, inicio, fim);
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(coordenador));
        when(reservaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaLaboratorioResponse resp = service.cancelar(1L, null, autCoordenador);
        assertThat(resp.status()).isEqualTo(StatusReserva.CANCELADA);
    }

    @Test
    void cancelar_reservaJaCancelada_lanca422() {
        ReservaLaboratorio reserva = reservaConfirmada(10L, AGORA.plusHours(1), AGORA.plusHours(2));
        reserva.setStatus(StatusReserva.CANCELADA);
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(1L, null, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void cancelar_reservaEncerrada_lanca422() {
        LocalDateTime inicio = AGORA.minusHours(3);
        LocalDateTime fim = AGORA.minusHours(1); // already ended
        ReservaLaboratorio reserva = reservaConfirmada(10L, inicio, fim);
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(1L, null, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void cancelar_usuarioComumNaoDono_lanca403() {
        ReservaLaboratorio reserva = reservaConfirmada(99L, AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findByIdWithDetails(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(1L, null, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(403));
    }

    // ---------- listar - roteamento por perfil ----------

    @Test
    void listar_professor_usaQueryParaProfessor() {
        UsuarioAutenticado autProfessor = new UsuarioAutenticado(2L, "prof@lab.com", "Prof", "PROFESSOR", false);
        when(reservaRepo.findWithFiltersParaProfessor(eq(2L), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.listar(autProfessor, null, null, null, null, null, 0);

        verify(reservaRepo).findWithFiltersParaProfessor(eq(2L), isNull(), isNull(), isNull(), isNull(), any());
        verify(reservaRepo, never()).findWithFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void listar_usuario_usaQueryComProprioIdEIgnoraFiltroExterno() {
        when(reservaRepo.findWithFilters(isNull(), eq(10L), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.listar(autUsuario, null, 99L, null, null, null, 0);

        verify(reservaRepo).findWithFilters(isNull(), eq(10L), isNull(), isNull(), isNull(), any());
        verify(reservaRepo, never()).findWithFiltersParaProfessor(any(), any(), any(), any(), any(), any());
    }

    // ---------- disponibilidade ----------

    @Test
    void disponibilidade_disponivel() {
        lab.setCapacidade(5);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of());

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isTrue();
        assertThat(resp.vagasRestantes()).isEqualTo(4); // capacity(5) - peak(1 new) = 4
        assertThat(resp.mensagem()).contains("vaga(s) restante(s)");
    }

    @Test
    void disponibilidade_lotado() {
        lab.setCapacidade(1);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        ReservaLaboratorio existente = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(existente));

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isFalse();
        assertThat(resp.vagasRestantes()).isEqualTo(0);
    }

    // ---------- disponibilidade × criar: mesma lógica (capacidade 2) ----------

    @Test
    void disponibilidade_capacidade2_semReservas_disponivel1Vaga() {
        lab.setCapacidade(2);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of());

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isTrue();
        assertThat(resp.vagasRestantes()).isEqualTo(1);
    }

    @Test
    void disponibilidade_capacidade2_umaReserva_ultimaVaga() {
        // BUG corrigido: capacidade=2, 1 existente → pico=2 → vagasRestantes=0 → deve ser disponível
        lab.setCapacidade(2);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        ReservaLaboratorio existente = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(existente));

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isTrue();
        assertThat(resp.vagasRestantes()).isEqualTo(0);
        assertThat(resp.mensagem()).contains("última vaga");
    }

    @Test
    void disponibilidade_capacidade2_duasReservas_lotado() {
        lab.setCapacidade(2);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        ReservaLaboratorio e1 = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        ReservaLaboratorio e2 = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(e1, e2));

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isFalse();
    }

    @Test
    void criar_capacidade2_umaReserva_cabe() {
        // Deve ser possível criar quando capacidade=2 e há 1 reserva existente (última vaga)
        lab.setCapacidade(2);
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        ReservaLaboratorio existente = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(existente));
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            ReservaLaboratorio r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        ReservaLaboratorioResponse resp = service.criar(req, autUsuario);
        assertThat(resp.status()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    void criar_capacidade2_duasReservas_lanca422() {
        lab.setCapacidade(2);
        LocalDateTime inicio = AGORA.plusHours(1);
        LocalDateTime fim = AGORA.plusHours(2);
        ReservaLaboratorioRequest req = new ReservaLaboratorioRequest(5L, null, inicio, fim, "Motivo", null);
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuarioComum));
        when(laboratorioRepo.findByIdWithLock(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasDoUsuarioSobrepostas(eq(10L), any(), any(), isNull()))
                .thenReturn(List.of());
        ReservaLaboratorio e1 = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        ReservaLaboratorio e2 = reservaComHorario(AGORA.plusHours(1), AGORA.plusHours(2));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of(e1, e2));

        assertThatThrownBy(() -> service.criar(req, autUsuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(status(e)).isEqualTo(422));
    }

    @Test
    void disponibilidade_cancelada_naoConta() {
        // findConfirmadasSobrepostas retorna vazio (canceladas não são CONFIRMADAS)
        lab.setCapacidade(1);
        when(laboratorioRepo.findById(5L)).thenReturn(Optional.of(lab));
        when(reservaRepo.findConfirmadasSobrepostas(eq(5L), any(), any())).thenReturn(List.of());

        DisponibilidadeResponse resp = service.verificarDisponibilidade(5L,
                AGORA.plusHours(1), AGORA.plusHours(2), autUsuario);
        assertThat(resp.disponivel()).isTrue();
        assertThat(resp.vagasRestantes()).isEqualTo(0); // cap(1) - pico(1 new) = 0 → última vaga
        assertThat(resp.mensagem()).contains("última vaga");
    }

    // ---------- helpers ----------

    private static Usuario usuario(Long id, String nome, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setPerfil(perfil);
        u.setAtivo(true);
        return u;
    }

    private static ReservaLaboratorio reservaConfirmada(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        ReservaLaboratorio r = new ReservaLaboratorio();
        r.setId(1L);
        Laboratorio l = new Laboratorio();
        l.setId(5L);
        l.setNome("Lab A");
        r.setLaboratorio(l);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNome("Usuario " + usuarioId);
        r.setUsuario(u);
        r.setDataInicio(inicio);
        r.setDataFim(fim);
        r.setStatus(StatusReserva.CONFIRMADA);
        r.setDataCriacao(LocalDateTime.now());
        return r;
    }

    private static ReservaLaboratorio reservaComHorario(LocalDateTime inicio, LocalDateTime fim) {
        ReservaLaboratorio r = new ReservaLaboratorio();
        r.setId(50L);
        r.setDataInicio(inicio);
        r.setDataFim(fim);
        r.setStatus(StatusReserva.CONFIRMADA);
        return r;
    }

    private static int status(Throwable e) {
        return ((ResponseStatusException) e).getStatusCode().value();
    }
}
