package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.PaginaResponse;
import com.main.gestaolabback.dto.ProjetoDetalheResponse;
import com.main.gestaolabback.dto.ProjetoRequest;
import com.main.gestaolabback.dto.ProjetoResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.TipoProjeto;
import com.main.gestaolabback.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @GetMapping
    public ResponseEntity<PaginaResponse<ProjetoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) TipoProjeto tipo,
            @RequestParam(required = false) Long orientadorId,
            @RequestParam(required = false) Long participanteId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                PaginaResponse.de(projetoService.listar(autenticado, busca, tipo, orientadorId, participanteId, ativo, page)));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<ProjetoResponse>> meusProjetos(
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(projetoService.meusProjetos(autenticado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDetalheResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(projetoService.buscarPorId(id, autenticado));
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @PostMapping
    public ResponseEntity<ProjetoResponse> criar(
            @Valid @RequestBody ProjetoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.criar(request, autenticado));
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProjetoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(projetoService.atualizar(id, request, autenticado));
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        projetoService.inativar(id, autenticado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        projetoService.reativar(id, autenticado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @PostMapping("/{id}/participantes")
    public ResponseEntity<ProjetoDetalheResponse> adicionarParticipante(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        Long usuarioId = body.get("usuarioId");
        return ResponseEntity.ok(projetoService.adicionarParticipante(id, usuarioId, autenticado));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ProjetoResponse>> projetosDeUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(projetoService.projetosAtivosDeUsuario(usuarioId));
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @DeleteMapping("/{id}/participantes/{usuarioId}")
    public ResponseEntity<ProjetoDetalheResponse> removerParticipante(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(projetoService.removerParticipante(id, usuarioId, autenticado));
    }
}
