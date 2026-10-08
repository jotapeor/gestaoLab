package com.main.gestaolabback.service;

import com.main.gestaolabback.config.AppConfig;
import com.main.gestaolabback.dto.AgendaLaboratorioResponse;
import com.main.gestaolabback.dto.CancelarReservaRequest;
import com.main.gestaolabback.dto.DisponibilidadeResponse;
import com.main.gestaolabback.dto.ReservaLaboratorioRequest;
import com.main.gestaolabback.dto.ReservaLaboratorioResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.model.Projeto;
import com.main.gestaolabback.model.ReservaLaboratorio;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.LaboratorioRepository;
import com.main.gestaolabback.repository.ProjetoRepository;
import com.main.gestaolabback.repository.ReservaLaboratorioRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservaLaboratorioService {

    private static final int PAGE_SIZE = 20;

    private final ReservaLaboratorioRepository reservaRepo;
    private final LaboratorioRepository laboratorioRepo;
    private final ProjetoRepository projetoRepo;
    private final UsuarioRepository usuarioRepo;
    private final AppConfig appConfig;
    private final Clock clock;

    public ReservaLaboratorioService(
            ReservaLaboratorioRepository reservaRepo,
            LaboratorioRepository laboratorioRepo,
            ProjetoRepository projetoRepo,
            UsuarioRepository usuarioRepo,
            AppConfig appConfig,
            Clock clock) {
        this.reservaRepo = reservaRepo;
        this.laboratorioRepo = laboratorioRepo;
        this.projetoRepo = projetoRepo;
        this.usuarioRepo = usuarioRepo;
        this.appConfig = appConfig;
        this.clock = clock;
    }

    public Page<ReservaLaboratorioResponse> listar(
            UsuarioAutenticado autenticado,
            Long laboratorioId, Long usuarioIdFiltro,
            StatusReserva status,
            LocalDateTime de, LocalDateTime ate,
            int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        if ("PROFESSOR".equals(autenticado.perfil())) {
            return reservaRepo.findWithFiltersParaProfessor(
                    autenticado.id(), laboratorioId, status, de, ate, pageable)
                    .map(ReservaLaboratorioResponse::de);
        }
        Long usuarioEfetivo = resolverFiltroUsuario(autenticado, usuarioIdFiltro);
        return reservaRepo.findWithFilters(laboratorioId, usuarioEfetivo, status, de, ate, pageable)
                .map(ReservaLaboratorioResponse::de);
    }

    @Transactional(readOnly = true)
    public ReservaLaboratorioResponse buscarPorId(Long id, UsuarioAutenticado autenticado) {
        ReservaLaboratorio r = reservaRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Reserva não encontrada."));
        verificarAcessoLeitura(r, autenticado);
        return ReservaLaboratorioResponse.de(r);
    }

    @Transactional
    public ReservaLaboratorioResponse criar(ReservaLaboratorioRequest request, UsuarioAutenticado autenticado) {
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!request.dataInicio().isAfter(agora)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "A data de início deve ser no futuro.");
        }
        if (!request.dataFim().isAfter(request.dataInicio())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "A data de fim deve ser após a data de início.");
        }

        long duracaoMinutos = Duration.between(request.dataInicio(), request.dataFim()).toMinutes();
        int minMinutos = appConfig.getReserva().getDuracaoMinimaMinutos();
        int maxHoras = appConfig.getReserva().getDuracaoMaximaHoras();
        if (duracaoMinutos < minMinutos) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "A duração mínima é de " + minMinutos + " minutos.");
        }
        if (duracaoMinutos > maxHoras * 60L) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "A duração máxima é de " + maxHoras + " horas.");
        }

        long diasAntecedencia = Duration.between(agora, request.dataInicio()).toDays();
        int maxDias = appConfig.getReserva().getAntecedenciaMaximaDias();
        if (diasAntecedencia > maxDias) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "A reserva pode ser feita com no máximo " + maxDias + " dias de antecedência.");
        }

        Usuario usuario = resolverUsuario(request, autenticado);

        Laboratorio laboratorio = laboratorioRepo.findByIdWithLock(request.laboratorioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Laboratório não encontrado."));
        if (!laboratorio.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Laboratório inativo.");
        }

        Projeto projeto = null;
        if (request.projetoId() != null) {
            projeto = projetoRepo.findByIdWithParticipantes(request.projetoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Projeto não encontrado."));
            if (!projeto.isAtivo()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Projeto inativo.");
            }
            final Long uid = usuario.getId();
            final Projeto p = projeto;
            boolean participa = p.getUsuarios().stream().anyMatch(u -> u.getId().equals(uid))
                    || (p.getOrientador() != null && p.getOrientador().getId().equals(uid));
            if (!participa && !"COORDENADOR".equals(autenticado.perfil())) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "O usuário não participa do projeto informado.");
            }
        } else {
            if (request.motivo() == null || request.motivo().isBlank()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Motivo é obrigatório quando não há projeto vinculado.");
            }
        }

        List<ReservaLaboratorio> sobrepostasUsuario = reservaRepo.findConfirmadasDoUsuarioSobrepostas(
                usuario.getId(), request.dataInicio(), request.dataFim(), null);
        if (!sobrepostasUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Você já tem uma reserva confirmada neste horário.");
        }

        if (laboratorio.getCapacidade() != null) {
            List<ReservaLaboratorio> sobrepostas = reservaRepo.findConfirmadasSobrepostas(
                    laboratorio.getId(), request.dataInicio(), request.dataFim());
            int pico = calcularPicoComNova(sobrepostas, request.dataInicio(), request.dataFim());
            if (pico > laboratorio.getCapacidade()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Laboratório lotado neste horário: capacidade de " + laboratorio.getCapacidade() + " pessoas.");
            }
        }

        ReservaLaboratorio reserva = new ReservaLaboratorio();
        reserva.setLaboratorio(laboratorio);
        reserva.setUsuario(usuario);
        reserva.setProjeto(projeto);
        reserva.setDataInicio(request.dataInicio());
        reserva.setDataFim(request.dataFim());
        reserva.setMotivo(request.motivo());
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setDataCriacao(agora);
        return ReservaLaboratorioResponse.de(reservaRepo.save(reserva));
    }

    @Transactional
    public ReservaLaboratorioResponse cancelar(Long id, CancelarReservaRequest request, UsuarioAutenticado autenticado) {
        ReservaLaboratorio reserva = reservaRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Reserva não encontrada."));

        LocalDateTime agora = LocalDateTime.now(clock);

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Reserva já está cancelada.");
        }
        if (!reserva.getDataFim().isAfter(agora)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Não é possível cancelar uma reserva já encerrada.");
        }

        boolean isCoordenador = "COORDENADOR".equals(autenticado.perfil());
        boolean isDono = reserva.getUsuario().getId().equals(autenticado.id());

        if (isCoordenador) {
            // coordenador pode cancelar qualquer momento antes do fim (já verificado acima)
        } else if (isDono) {
            if (!reserva.getDataInicio().isAfter(agora)) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Você só pode cancelar a reserva antes do início.");
            }
        } else {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        }

        Usuario canceladoPor = usuarioRepo.findById(autenticado.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setCanceladoPor(canceladoPor);
        reserva.setDataCancelamento(agora);
        reserva.setMotivoCancelamento(request != null ? request.motivo() : null);
        return ReservaLaboratorioResponse.de(reservaRepo.save(reserva));
    }

    public DisponibilidadeResponse verificarDisponibilidade(Long laboratorioId,
                                                             LocalDateTime inicio, LocalDateTime fim,
                                                             UsuarioAutenticado autenticado) {
        Laboratorio laboratorio = laboratorioRepo.findById(laboratorioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Laboratório não encontrado."));

        if (!laboratorio.isAtivo()) {
            return new DisponibilidadeResponse(false, null, "Laboratório inativo.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        if (inicio == null || fim == null) {
            return new DisponibilidadeResponse(false, null, "Datas inválidas.");
        }
        if (!inicio.isAfter(agora)) {
            return new DisponibilidadeResponse(false, null, "Data de início deve ser no futuro.");
        }
        if (!fim.isAfter(inicio)) {
            return new DisponibilidadeResponse(false, null, "Data de fim deve ser após o início.");
        }

        long duracaoMinutos = Duration.between(inicio, fim).toMinutes();
        int minMinutos = appConfig.getReserva().getDuracaoMinimaMinutos();
        int maxHoras = appConfig.getReserva().getDuracaoMaximaHoras();
        if (duracaoMinutos < minMinutos) {
            return new DisponibilidadeResponse(false, null, "Duração mínima: " + minMinutos + " minutos.");
        }
        if (duracaoMinutos > maxHoras * 60L) {
            return new DisponibilidadeResponse(false, null, "Duração máxima: " + maxHoras + " horas.");
        }

        if (laboratorio.getCapacidade() == null) {
            return new DisponibilidadeResponse(true, null, "Disponível (sem limite de capacidade).");
        }

        List<ReservaLaboratorio> sobrepostas = reservaRepo.findConfirmadasSobrepostas(laboratorioId, inicio, fim);
        int pico = calcularPicoComNova(sobrepostas, inicio, fim);
        int vagas = laboratorio.getCapacidade() - pico;

        if (vagas <= 0) {
            return new DisponibilidadeResponse(false, 0,
                    "Laboratório lotado neste horário: capacidade de " + laboratorio.getCapacidade() + " pessoas.");
        }
        return new DisponibilidadeResponse(true, vagas, "Disponível – " + vagas + " vaga(s) restante(s).");
    }

    @Transactional(readOnly = true)
    public AgendaLaboratorioResponse agenda(Long laboratorioId, LocalDateTime de, LocalDateTime ate,
                                             UsuarioAutenticado autenticado) {
        if (de == null || ate == null || !ate.isAfter(de)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Intervalo de datas inválido.");
        }
        if (Duration.between(de, ate).toDays() > 31) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O intervalo máximo para agenda é de 31 dias.");
        }

        Laboratorio laboratorio = laboratorioRepo.findById(laboratorioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Laboratório não encontrado."));

        List<ReservaLaboratorio> reservas = reservaRepo.findConfirmadasParaAgenda(laboratorioId, de, ate);

        boolean isCoordenador = "COORDENADOR".equals(autenticado.perfil());
        Long userId = autenticado.id();

        List<AgendaLaboratorioResponse.BlocoAgenda> blocos = reservas.stream().map(r -> {
            boolean podVerNomes = isCoordenador || r.getUsuario().getId().equals(userId);
            String nomeUsuario = podVerNomes ? r.getUsuario().getNome() : null;
            String nomeProjeto = (podVerNomes && r.getProjeto() != null) ? r.getProjeto().getTitulo() : null;
            int ocupacao = (int) reservas.stream()
                    .filter(other -> other.getDataInicio().isBefore(r.getDataFim())
                            && other.getDataFim().isAfter(r.getDataInicio()))
                    .count();
            return new AgendaLaboratorioResponse.BlocoAgenda(
                    r.getId(), r.getDataInicio(), r.getDataFim(), nomeUsuario, nomeProjeto, ocupacao);
        }).toList();

        return new AgendaLaboratorioResponse(laboratorio.getCapacidade(), blocos);
    }

    private int calcularPicoComNova(List<ReservaLaboratorio> existentes,
                                     LocalDateTime inicio, LocalDateTime fim) {
        List<long[]> eventos = new ArrayList<>();
        for (ReservaLaboratorio r : existentes) {
            eventos.add(new long[]{r.getDataInicio().toEpochSecond(ZoneOffset.UTC), +1});
            eventos.add(new long[]{r.getDataFim().toEpochSecond(ZoneOffset.UTC), -1});
        }
        eventos.add(new long[]{inicio.toEpochSecond(ZoneOffset.UTC), +1});
        eventos.add(new long[]{fim.toEpochSecond(ZoneOffset.UTC), -1});
        // sort by time; at same time, end event (-1) before start event (+1)
        eventos.sort(Comparator.comparingLong((long[] e) -> e[0]).thenComparingLong(e -> e[1]));
        int max = 0, current = 0;
        for (long[] e : eventos) {
            current += (int) e[1];
            if (current > max) max = current;
        }
        return max;
    }

    private Long resolverFiltroUsuario(UsuarioAutenticado autenticado, Long usuarioIdFiltro) {
        return switch (autenticado.perfil()) {
            case "USUARIO" -> autenticado.id();
            default -> usuarioIdFiltro; // COORDENADOR: null = sem restrição de usuário
        };
    }

    private void verificarAcessoLeitura(ReservaLaboratorio r, UsuarioAutenticado autenticado) {
        if ("COORDENADOR".equals(autenticado.perfil())) return;
        if (r.getUsuario().getId().equals(autenticado.id())) return;
        if ("PROFESSOR".equals(autenticado.perfil())) {
            if (r.getProjeto() != null && r.getProjeto().getOrientador() != null
                    && r.getProjeto().getOrientador().getId().equals(autenticado.id())) return;
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
    }

    private Usuario resolverUsuario(ReservaLaboratorioRequest request, UsuarioAutenticado autenticado) {
        Long uid;
        if (request.usuarioId() != null) {
            if (!"COORDENADOR".equals(autenticado.perfil())) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                        "Somente coordenadores podem reservar em nome de outro usuário.");
            }
            uid = request.usuarioId();
        } else {
            uid = autenticado.id();
        }
        return usuarioRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));
    }
}
