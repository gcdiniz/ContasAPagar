package com.desafio.contaspagar.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SituacaoContaTest {

    @ParameterizedTest(name = "De {0} para {1} deve ser {2}")
    @MethodSource("transicoesProvider")
    @DisplayName("Deve validar transições de situação")
    void deveValidarTransicoes(SituacaoConta atual, SituacaoConta nova, boolean esperado) {
        assertEquals(esperado, atual.podeTransicionar(nova));
    }

    static Stream<Arguments> transicoesProvider() {
        return Stream.of(
                // PENDENTE pode ir para PAGO ou CANCELADO
                Arguments.of(SituacaoConta.PENDENTE, SituacaoConta.PAGO, true),
                Arguments.of(SituacaoConta.PENDENTE, SituacaoConta.CANCELADO, true),
                Arguments.of(SituacaoConta.PENDENTE, SituacaoConta.PENDENTE, false),

                // PAGO não pode ir para nenhum estado
                Arguments.of(SituacaoConta.PAGO, SituacaoConta.PENDENTE, false),
                Arguments.of(SituacaoConta.PAGO, SituacaoConta.CANCELADO, false),
                Arguments.of(SituacaoConta.PAGO, SituacaoConta.PAGO, false),

                // CANCELADO não pode ir para nenhum estado
                Arguments.of(SituacaoConta.CANCELADO, SituacaoConta.PENDENTE, false),
                Arguments.of(SituacaoConta.CANCELADO, SituacaoConta.PAGO, false),
                Arguments.of(SituacaoConta.CANCELADO, SituacaoConta.CANCELADO, false)
        );
    }

    @Test
    @DisplayName("Conta PENDENTE deve poder ser PAGA")
    void contaPendentePodeSerpaga() {
        assertTrue(SituacaoConta.PENDENTE.podeTransicionar(SituacaoConta.PAGO));
    }

    @Test
    @DisplayName("Conta PAGA não pode voltar a PENDENTE")
    void contaPagaNaoPodeVoltarPendente() {
        assertFalse(SituacaoConta.PAGO.podeTransicionar(SituacaoConta.PENDENTE));
    }
}
