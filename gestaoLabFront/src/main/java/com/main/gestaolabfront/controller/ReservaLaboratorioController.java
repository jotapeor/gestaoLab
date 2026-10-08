package com.main.gestaolabfront.controller;

import tools.jackson.databind.ObjectMapper;
import com.main.gestaolabfront.dto.AgendaDto;
import com.main.gestaolabfront.dto.ApiErrorDto;
import com.main.gestaolabfront.dto.DisponibilidadeDto;
import com.main.gestaolabfront.dto.LaboratorioDto;
import com.main.gestaolabfront.dto.PaginaResponse;
import com.main.gestaolabfront.dto.ReservaLaboratorioDto;
import com.main.gestaolabfront.service.LaboratorioApiService;
import com.main.gestaolabfront.service.ReservaLaboratorioApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaLaboratorioController {

    private final ReservaLaboratorioApiService reservaApiService;
    private final LaboratorioApiService laboratorioApiService;
    private final ObjectMapper objectMapper;

    public ReservaLaboratorioController(ReservaLaboratorioApiService reservaApiService,
                                         LaboratorioApiService laboratorioApiService,
                                         ObjectMapper objectMapper) {
        this.reservaApiService = reservaApiService;
        this.laboratorioApiService = laboratorioApiService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String listar(HttpSession session, Model model,
                         @RequestParam(required = false) Long laboratorioId,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                         @RequestParam(defaultValue = "0") int page) {
        try {
            LocalDateTime dtDe  = de  != null ? de.atStartOfDay() : null;
            LocalDateTime dtAte = ate != null ? ate.atTime(LocalTime.MAX) : null;
            PaginaResponse<ReservaLaboratorioDto> pagina = reservaApiService.listar(
                    laboratorioId, null, status, dtDe, dtAte, page);
            model.addAttribute("pagina", pagina);
        } catch (Exception e) {
            model.addAttribute("pagina", new PaginaResponse<ReservaLaboratorioDto>(List.of(), 0, 0, 0L, 0));
        }

        try {
            model.addAttribute("laboratorios", laboratorioApiService.listar(null, true));
        } catch (Exception e) {
            model.addAttribute("laboratorios", List.of());
        }

        model.addAttribute("menuAtivo", "reservas");
        model.addAttribute("filtroLaboratorioId", laboratorioId);
        model.addAttribute("filtroStatus", status);
        model.addAttribute("filtroDe", de);
        model.addAttribute("filtroAte", ate);
        model.addAttribute("pageAtual", page);
        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("urlVoltar", "/dashboard");
        return "reservas/lista";
    }

    @GetMapping("/nova")
    public String novaForm(HttpSession session, Model model) {
        try {
            model.addAttribute("laboratorios", laboratorioApiService.listar(null, true));
        } catch (Exception e) {
            model.addAttribute("laboratorios", List.of());
        }
        model.addAttribute("menuAtivo", "reservas");
        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("urlVoltar", "/reservas");
        return "reservas/form";
    }

    @PostMapping("/salvar")
    public String salvar(
            @RequestParam Long laboratorioId,
            @RequestParam(required = false) Long projetoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Long usuarioId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Long uidParaReservar = isCoordenador(session) ? usuarioId : null;
            ReservaLaboratorioDto criada = reservaApiService.criar(
                    laboratorioId, projetoId, dataInicio, dataFim, motivo, uidParaReservar);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Reserva registrada com sucesso!");
            return "redirect:/reservas/" + criada.id();
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao registrar reserva."));
            redirectAttributes.addFlashAttribute("formLaboratorioId", laboratorioId);
            redirectAttributes.addFlashAttribute("formDataInicio", dataInicio);
            redirectAttributes.addFlashAttribute("formDataFim", dataFim);
            redirectAttributes.addFlashAttribute("formMotivo", motivo);
            return "redirect:/reservas/nova";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao registrar reserva.");
            return "redirect:/reservas/nova";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, HttpSession session, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("reserva", reservaApiService.buscarPorId(id));
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reserva não encontrada.");
            return "redirect:/reservas";
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao carregar reserva.");
            return "redirect:/reservas";
        }
        model.addAttribute("menuAtivo", "reservas");
        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("urlVoltar", "/reservas");
        return "reservas/detalhe";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(required = false) String motivo,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            reservaApiService.cancelar(id, motivo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Reserva cancelada com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao cancelar reserva."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao cancelar reserva.");
        }
        return "redirect:/reservas/" + id;
    }

    @GetMapping("/agenda/{laboratorioId}")
    public String agenda(@PathVariable Long laboratorioId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                         HttpSession session, Model model,
                         RedirectAttributes redirectAttributes) {
        LocalDate inicio = de != null ? de : LocalDate.now();
        LocalDate fim = inicio.plusDays(6);
        LocalDateTime dtDe  = inicio.atStartOfDay();
        LocalDateTime dtAte = fim.atTime(LocalTime.MAX);

        LaboratorioDto lab;
        try {
            lab = laboratorioApiService.buscarPorId(laboratorioId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Laboratório não encontrado.");
            return "redirect:/reservas";
        }

        AgendaDto agenda;
        try {
            agenda = reservaApiService.agenda(laboratorioId, dtDe, dtAte);
        } catch (Exception e) {
            agenda = new AgendaDto(null, List.of());
        }

        model.addAttribute("laboratorio", lab);
        model.addAttribute("agenda", agenda);
        model.addAttribute("semanaInicio", inicio);
        model.addAttribute("semanaFim", fim);
        model.addAttribute("menuAtivo", "reservas");
        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("urlVoltar", "/reservas");
        return "reservas/agenda";
    }

    @GetMapping("/disponibilidade")
    @ResponseBody
    public DisponibilidadeDto disponibilidade(
            @RequestParam Long laboratorioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        try {
            return reservaApiService.verificarDisponibilidade(laboratorioId, inicio, fim);
        } catch (Exception e) {
            return new DisponibilidadeDto(false, null, "Erro ao verificar disponibilidade.");
        }
    }

    private boolean isCoordenador(HttpSession session) {
        return "COORDENADOR".equals(session.getAttribute("perfil"));
    }

    private String extrairMensagem(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) return fallback;
        try {
            ApiErrorDto error = objectMapper.readValue(responseBody, ApiErrorDto.class);
            return error.message() != null ? error.message() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
