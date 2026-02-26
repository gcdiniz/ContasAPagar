package com.desafio.contaspagar.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue.csv-import}")
    private String queueName;

    @Value("${app.rabbitmq.exchange.csv-import}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.csv-import}")
    private String routingKey;

    @Bean
    public Queue csvImportQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange csvImportExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding csvImportBinding(Queue csvImportQueue, DirectExchange csvImportExchange) {
        return BindingBuilder.bind(csvImportQueue).to(csvImportExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
