package com.jvnlee.catchdining.common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    public static final String REVIEW_EVENT_QUEUE = "REVIEW_EVENT_QUEUE";

    public static final String RESTAURANT_EVENT_QUEUE = "RESTAURANT_EVENT_QUEUE";

    public static final String RESERVATION_EVENT_QUEUE = "RESERVATION_EVENT_QUEUE";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue reviewEventQueue() {
        return new Queue(REVIEW_EVENT_QUEUE);
    }

    @Bean
    public Queue restaurantEventQueue() {
        return new Queue(RESTAURANT_EVENT_QUEUE);
    }

    @Bean
    public Queue reservationEventQueue() {
        return new Queue(RESERVATION_EVENT_QUEUE);
    }

}
