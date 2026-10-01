package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Value("${api.security.token.secret}")
    private String secret;

    @Test
    void gerarToken_eValidar_comSucesso() {
        Usuario usuario = buildTestUser();
        String token = tokenService.gerarToken(usuario);

        assertThat(tokenService.validarToken(token)).isTrue();

        UsuarioAutenticado claims = tokenService.extrairClaims(token);
        assertThat(claims.id()).isEqualTo(1L);
        assertThat(claims.email()).isEqualTo("test@gestaolab.local");
        assertThat(claims.nome()).isEqualTo("Usuário Teste");
        assertThat(claims.perfil()).isEqualTo("COORDENADOR");
        assertThat(claims.primeiroAcesso()).isTrue();
    }

    @Test
    void validarToken_tokenAdulterado_retornaFalse() {
        String token = tokenService.gerarToken(buildTestUser());
        String adulterado = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(tokenService.validarToken(adulterado)).isFalse();
    }

    @Test
    void validarToken_tokenExpirado_retornaFalse() {
        String expiredToken = Jwts.builder()
                .subject("test@gestaolab.local")
                .claim("id", 1L)
                .claim("nome", "Usuário Teste")
                .claim("perfil", "COORDENADOR")
                .claim("primeiroAcesso", true)
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .compact();

        assertThat(tokenService.validarToken(expiredToken)).isFalse();
    }

    private Usuario buildTestUser() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("test@gestaolab.local");
        u.setNome("Usuário Teste");
        u.setSenha("$2a$10$hashedpassword");
        u.setPerfil(PerfilUsuario.COORDENADOR);
        u.setPrimeiroAcesso(true);
        return u;
    }
}
