package com.main.gestaolabback.dto;

public record LoginResponse(String token, String nome, String perfil, boolean primeiroAcesso) {
}
