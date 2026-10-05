package com.main.gestaolabback.dto;

import com.main.gestaolabback.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 50, message = "Matrícula deve ter no máximo 50 caracteres")
        String matricula,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        PerfilUsuario perfil,

        Long cursoSetorId,

        Long responsavelId,

        @Size(min = 8, message = "Senha provisória deve ter no mínimo 8 caracteres")
        String senhaProvisoria
) {}
