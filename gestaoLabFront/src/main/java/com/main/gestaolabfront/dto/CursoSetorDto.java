package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;

public record CursoSetorDto(Long id, String nome, String tipo, boolean ativo, LocalDateTime dataCadastro) {}
