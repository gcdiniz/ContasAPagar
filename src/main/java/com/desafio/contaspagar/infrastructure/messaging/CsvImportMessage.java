package com.desafio.contaspagar.infrastructure.messaging;

import java.io.Serializable;

public record CsvImportMessage(
        String protocolo,
        String csvContent
) implements Serializable {
}
