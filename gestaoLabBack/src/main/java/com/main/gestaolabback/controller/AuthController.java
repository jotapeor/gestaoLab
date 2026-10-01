package com.main.gestaolabback.controller;

import com.main.gestaolabback.dto.LoginRequest;
import com.main.gestaolabback.dto.LoginResponse;
import com.main.gestaolabback.dto.TrocarSenhaRequest;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/autenticar")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/logar")
    public ResponseEntity<LoginResponse> logar(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.logar(request));
    }

    @PostMapping("/trocar-senha")
    public ResponseEntity<LoginResponse> trocarSenha(
            @Valid @RequestBody TrocarSenhaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        return ResponseEntity.ok(authService.trocarSenha(request, usuarioAutenticado.id()));
    }
}
