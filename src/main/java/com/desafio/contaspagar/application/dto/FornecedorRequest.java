package com.desafio.contaspagar.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank(message = "Nome do fornecedor é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome
) {
}
