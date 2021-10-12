package com.azierets.restapijwt.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String rabbitMqHost;

    @Value("${spring.rabbitmq.port}")
    private Integer rabbitMqPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitMqUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitMqPassword;

    @Value("${spring.rabbitmq.template.exchange}")
    private String rabbitMqExchangeName;

    @Value("${spring.rabbitmq.template.queue-name}")
    private String rabbitMqQueueName;

    @Bean
    public Queue loggingQueue() {
        return new Queue(rabbitMqQueueName, true);
    }

    @Bean
    public FanoutExchange loggingExchange() {
        return new FanoutExchange(rabbitMqExchangeName);
    }

    @Bean
    public Binding loggingBinding(Queue loggingQueue, FanoutExchange exchange) {
        return BindingBuilder.bind(loggingQueue).to(exchange);
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitMqHost);
        cachingConnectionFactory.setPort(rabbitMqPort);
        cachingConnectionFactory.setUsername(rabbitMqUser);
        cachingConnectionFactory.setPassword(rabbitMqPassword);
        return cachingConnectionFactory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setExchange(loggingExchange().getName());
        rabbitTemplate.setRoutingKey(loggingQueue().getName());
        return rabbitTemplate;
    }
}
