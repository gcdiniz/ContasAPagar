package com.desafio.contaspagar.infrastructure.messaging;

import com.desafio.contaspagar.infrastructure.batch.CsvImportJobLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer RabbitMQ que recebe mensagens de importação CSV
 * e delega o processamento ao Spring Batch via CsvImportJobLauncher.
 */
@Component
public class CsvImportConsumer {

    private static final Logger log = LoggerFactory.getLogger(CsvImportConsumer.class);

    private final CsvImportJobLauncher csvImportJobLauncher;

    public CsvImportConsumer(CsvImportJobLauncher csvImportJobLauncher) {
        this.csvImportJobLauncher = csvImportJobLauncher;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.csv-import}")
    public void consume(CsvImportMessage message) {
        log.info("Mensagem recebida - Protocolo: {}. Delegando ao Spring Batch...", message.protocolo());
        csvImportJobLauncher.executar(message.protocolo(), message.csvContent());
    }
}
