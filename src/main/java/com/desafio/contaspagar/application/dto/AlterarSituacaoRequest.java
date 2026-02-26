package com.desafio.contaspagar.application.dto;

import com.desafio.contaspagar.domain.enums.SituacaoConta;
import jakarta.validation.constraints.NotNull;

public record AlterarSituacaoRequest(
        @NotNull(message = "Nova situação é obrigatória")
        SituacaoConta situacao
) {
}
