package com.desafio.contaspagar.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Usuário é obrigatório")
        String usuario,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {
}
