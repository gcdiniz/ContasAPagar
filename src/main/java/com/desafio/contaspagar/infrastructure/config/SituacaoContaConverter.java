package com.desafio.contaspagar.infrastructure.config;

import com.desafio.contaspagar.domain.enums.SituacaoConta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SituacaoContaConverter implements AttributeConverter<SituacaoConta, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SituacaoConta situacao) {
        return situacao == null ? null : situacao.getCodigo();
    }

    @Override
    public SituacaoConta convertToEntityAttribute(Integer codigo) {
        return codigo == null ? null : SituacaoConta.fromCodigo(codigo);
    }
}
