package com.desafio.contaspagar.infrastructure.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

/**
 * Listener que loga o resultado da execução do Job de importação CSV,
 * incluindo contagem de linhas processadas, escritas e puladas.
 */
public class CsvJobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CsvJobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String protocolo = jobExecution.getJobParameters().getString("protocolo");
        log.info("Iniciando Job de importação CSV - Protocolo: {}", protocolo);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String protocolo = jobExecution.getJobParameters().getString("protocolo");
        BatchStatus status = jobExecution.getStatus();

        int readCount = 0;
        int writeCount = 0;
        int skipCount = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            readCount += stepExecution.getReadCount();
            writeCount += stepExecution.getWriteCount();
            skipCount += stepExecution.getSkipCount();
        }

        log.info("Job finalizado - Protocolo: {} | Status: {} | Lidas: {} | Salvas: {} | Puladas: {}",
                protocolo, status, readCount, writeCount, skipCount);

        if (status == BatchStatus.FAILED) {
            jobExecution.getAllFailureExceptions().forEach(ex ->
                    log.error("Protocolo {} | Falha no Job: {}", protocolo, ex.getMessage())
            );
        }
    }
}
