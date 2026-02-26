package com.desafio.contaspagar.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaRequest(
        @NotNull(message = "Data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
        @Digits(integer = 13, fraction = 2, message = "Valor deve ter no máximo 13 dígitos inteiros e 2 decimais")
        BigDecimal valor,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "ID do fornecedor é obrigatório")
        Long fornecedorId
) {
}
