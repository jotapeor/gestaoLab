package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.AgendaLaboratorioResponse;
import com.main.gestaolabback.dto.LaboratorioRequest;
import com.main.gestaolabback.dto.LaboratorioResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.service.LaboratorioService;
import com.main.gestaolabback.service.ReservaLaboratorioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioService laboratorioService;
    private final ReservaLaboratorioService reservaService;

    public LaboratorioController(LaboratorioService laboratorioService,
                                  ReservaLaboratorioService reservaService) {
        this.laboratorioService = laboratorioService;
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<LaboratorioResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(laboratorioService.listar(nome, ativo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboratorioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(laboratorioService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PostMapping
    public ResponseEntity<LaboratorioResponse> criar(@Valid @RequestBody LaboratorioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratorioService.criar(request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<LaboratorioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LaboratorioRequest request) {
        return ResponseEntity.ok(laboratorioService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        laboratorioService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        laboratorioService.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/agenda")
    public ResponseEntity<AgendaLaboratorioResponse> agenda(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(reservaService.agenda(id, de, ate, autenticado));
    }
}
