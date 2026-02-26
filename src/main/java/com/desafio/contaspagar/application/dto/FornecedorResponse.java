package com.desafio.contaspagar.application.dto;

import com.desafio.contaspagar.domain.entity.Fornecedor;

public record FornecedorResponse(
        Long id,
        String nome
) {
    public static FornecedorResponse fromEntity(Fornecedor fornecedor) {
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getNome());
    }
}
