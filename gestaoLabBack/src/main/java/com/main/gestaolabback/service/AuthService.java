package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.LoginRequest;
import com.main.gestaolabback.dto.LoginResponse;
import com.main.gestaolabback.dto.TrocarSenhaRequest;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse logar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha inválidos.");
        }

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Usuário inativo.");
        }

        return new LoginResponse(
                tokenService.gerarToken(usuario),
                usuario.getNome(),
                usuario.getPerfil().name(),
                usuario.isPrimeiroAcesso()
        );
    }

    public LoginResponse trocarSenha(TrocarSenhaRequest request, Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Senha atual incorreta.");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "A nova senha não pode ser igual à senha atual.");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuario.setPrimeiroAcesso(false);
        usuarioRepository.save(usuario);

        return new LoginResponse(
                tokenService.gerarToken(usuario),
                usuario.getNome(),
                usuario.getPerfil().name(),
                false
        );
    }
}
