package com.main.gestaolabfront.dto;

import java.time.LocalDateTime;

public record LaboratorioDto(Long id, String nome, String localizacao, Integer capacidade,
                              boolean ativo, LocalDateTime dataCadastro) {}
