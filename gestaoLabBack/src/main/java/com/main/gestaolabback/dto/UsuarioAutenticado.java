package com.main.gestaolabback.dto;

public record UsuarioAutenticado(Long id, String email, String nome, String perfil, boolean primeiroAcesso) {
}
