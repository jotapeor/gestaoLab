package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.LaboratorioRequest;
import com.main.gestaolabback.dto.LaboratorioResponse;
import com.main.gestaolabback.service.LaboratorioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioService laboratorioService;

    public LaboratorioController(LaboratorioService laboratorioService) {
        this.laboratorioService = laboratorioService;
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
}
