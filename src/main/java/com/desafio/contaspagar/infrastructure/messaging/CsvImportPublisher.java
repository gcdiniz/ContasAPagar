package com.desafio.contaspagar.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CsvImportPublisher {

    private static final Logger log = LoggerFactory.getLogger(CsvImportPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public CsvImportPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange.csv-import}") String exchange,
            @Value("${app.rabbitmq.routing-key.csv-import}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(String protocolo, String csvContent) {
        CsvImportMessage message = new CsvImportMessage(protocolo, csvContent);
        log.info("Publicando mensagem de importação CSV - Protocolo: {}", protocolo);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("Mensagem publicada com sucesso - Protocolo: {}", protocolo);
    }
}
