package com.main.gestaolabfront.dto;

public record LoginResponse(String token, String nome, String perfil, boolean primeiroAcesso) {}
