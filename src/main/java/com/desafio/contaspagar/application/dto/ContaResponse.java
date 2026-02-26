package com.desafio.contaspagar.application.dto;

import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.domain.enums.SituacaoConta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaResponse(
        UUID id,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        BigDecimal valor,
        String descricao,
        SituacaoConta situacao,
        FornecedorResponse fornecedor
) {
    public static ContaResponse fromEntity(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getDataVencimento(),
                conta.getDataPagamento(),
                conta.getValor(),
                conta.getDescricao(),
                conta.getSituacao(),
                new FornecedorResponse(
                        conta.getFornecedor().getId(),
                        conta.getFornecedor().getNome()
                )
        );
    }
}
