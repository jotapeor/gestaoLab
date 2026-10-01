package com.main.gestaolabback.config;

import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.repository.UsuarioRepository;
import com.main.gestaolabback.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @Value("${api.security.session.inactivity-timeout-ms:1800000}")
    private long inactivityTimeoutMs;

    private static final ConcurrentHashMap<Long, Instant> lastActivity = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (tokenService.validarToken(token)) {
                    UsuarioAutenticado usuario = tokenService.extrairClaims(token);

                    usuarioRepository.findById(usuario.id()).ifPresent(u -> {
                        if (u.isAtivo()) {
                            Long userId = usuario.id();
                            Instant now = Instant.now();
                            Instant lastSeen = lastActivity.get(userId);

                            if (lastSeen != null && now.toEpochMilli() - lastSeen.toEpochMilli() > inactivityTimeoutMs) {
                                lastActivity.remove(userId);
                            } else {
                                lastActivity.put(userId, now);
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.perfil().toUpperCase());
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(usuario, null, Collections.singletonList(authority));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("[JWT] Token inválido: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
