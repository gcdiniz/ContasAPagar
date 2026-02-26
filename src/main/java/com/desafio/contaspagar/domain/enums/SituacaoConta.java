package com.desafio.contaspagar.domain.enums;

import java.util.Arrays;

public enum SituacaoConta {
    PENDENTE(1),
    PAGO(2),
    CANCELADO(3);

    private final int codigo;

    SituacaoConta(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static SituacaoConta fromCodigo(int codigo) {
        return Arrays.stream(values())
                .filter(s -> s.codigo == codigo)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código de situação inválido: " + codigo));
    }

    public boolean podeTransicionar(SituacaoConta novaSituacao) {
        return switch (this) {
            case PENDENTE -> novaSituacao == PAGO || novaSituacao == CANCELADO;
            case PAGO -> false;
            case CANCELADO -> false;
        };
    }
}
