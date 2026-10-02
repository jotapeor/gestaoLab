package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.CursoSetorRequest;
import com.main.gestaolabback.dto.CursoSetorResponse;
import com.main.gestaolabback.model.TipoCursoSetor;
import com.main.gestaolabback.service.CursoSetorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos-setores")
public class CursoSetorController {

    private final CursoSetorService cursoSetorService;

    public CursoSetorController(CursoSetorService cursoSetorService) {
        this.cursoSetorService = cursoSetorService;
    }

    @GetMapping
    public ResponseEntity<List<CursoSetorResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) TipoCursoSetor tipo) {
        return ResponseEntity.ok(cursoSetorService.listar(nome, ativo, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoSetorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cursoSetorService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PostMapping
    public ResponseEntity<CursoSetorResponse> criar(@Valid @RequestBody CursoSetorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoSetorService.criar(request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<CursoSetorResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CursoSetorRequest request) {
        return ResponseEntity.ok(cursoSetorService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        cursoSetorService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        cursoSetorService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
