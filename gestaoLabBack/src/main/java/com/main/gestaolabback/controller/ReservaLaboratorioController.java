package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.AgendaLaboratorioResponse;
import com.main.gestaolabback.dto.CancelarReservaRequest;
import com.main.gestaolabback.dto.DisponibilidadeResponse;
import com.main.gestaolabback.dto.PaginaResponse;
import com.main.gestaolabback.dto.ReservaLaboratorioRequest;
import com.main.gestaolabback.dto.ReservaLaboratorioResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.StatusReserva;
import com.main.gestaolabback.service.ReservaLaboratorioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservas")
public class ReservaLaboratorioController {

    private final ReservaLaboratorioService reservaService;

    public ReservaLaboratorioController(ReservaLaboratorioService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ReservaLaboratorioResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @RequestParam(required = false) Long laboratorioId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) StatusReserva status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(PaginaResponse.de(
                reservaService.listar(autenticado, laboratorioId, usuarioId, status, de, ate, page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaLaboratorioResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(reservaService.buscarPorId(id, autenticado));
    }

    @PostMapping
    public ResponseEntity<ReservaLaboratorioResponse> criar(
            @Valid @RequestBody ReservaLaboratorioRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.criar(request, autenticado));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaLaboratorioResponse> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CancelarReservaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(reservaService.cancelar(id, request, autenticado));
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> disponibilidade(
            @RequestParam Long laboratorioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(
                reservaService.verificarDisponibilidade(laboratorioId, inicio, fim, autenticado));
    }
}
