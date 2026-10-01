package com.main.gestaolabfront.exception;

public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException() {
        super("Sua sessão expirou. Faça login novamente.");
    }
}
