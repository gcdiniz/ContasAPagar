package com.desafio.contaspagar.infrastructure.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

/**
 * Listener do Spring Batch que captura e loga erros de linhas
 * puladas (skip) durante o processamento do CSV.
 * Garante visibilidade das falhas parciais sem interromper o job.
 */
public class CsvSkipListener implements SkipListener<ContaCsvRow, Object> {

    private static final Logger log = LoggerFactory.getLogger(CsvSkipListener.class);

    private final String protocolo;

    public CsvSkipListener(String protocolo) {
        this.protocolo = protocolo;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException e) {
            log.warn("Protocolo {} | Erro ao ler linha {}: {}",
                    protocolo, e.getLineNumber(), e.getInput());
        } else {
            log.warn("Protocolo {} | Erro ao ler linha do CSV: {}", protocolo, t.getMessage());
        }
    }

    @Override
    public void onSkipInProcess(ContaCsvRow item, Throwable t) {
        log.warn("Protocolo {} | Erro ao processar linha (descrição: '{}', fornecedorId: {}): {}",
                protocolo,
                item.getDescricao(),
                item.getFornecedorId(),
                t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Protocolo {} | Erro ao salvar conta: {}", protocolo, t.getMessage());
    }
}
