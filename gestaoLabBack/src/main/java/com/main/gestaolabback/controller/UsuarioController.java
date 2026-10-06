package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.MeuPerfilRequest;
import com.main.gestaolabback.dto.PaginaResponse;
import com.main.gestaolabback.dto.ParticipanteDisponivelResponse;
import com.main.gestaolabback.dto.ResponsavelResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.dto.UsuarioRequest;
import com.main.gestaolabback.dto.UsuarioResponse;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @GetMapping
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) PerfilUsuario perfil,
            @RequestParam(required = false) Long cursoSetorId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                PaginaResponse.de(usuarioService.listar(autenticado, busca, perfil, cursoSetorId, ativo, page)));
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id, autenticado));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        usuarioService.inativar(id, autenticado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        usuarioService.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @PatchMapping("/{id}/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String novaSenha = body.get("novaSenhaProvisoria");
        usuarioService.redefinirSenha(id, novaSenha);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COORDENADOR')")
    @GetMapping("/responsaveis")
    public ResponseEntity<List<ResponsavelResponse>> listarResponsaveis() {
        return ResponseEntity.ok(usuarioService.listarResponsaveis());
    }

    @PreAuthorize("hasAnyRole('COORDENADOR', 'PROFESSOR')")
    @GetMapping("/participantes-disponiveis")
    public ResponseEntity<List<ParticipanteDisponivelResponse>> listarParticipantesDisponiveis(
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(usuarioService.listarParticipantesDisponiveis(busca));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(usuarioService.me(autenticado.id()));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarMe(
            @Valid @RequestBody MeuPerfilRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(usuarioService.atualizarMe(autenticado.id(), request));
    }
}
