package com.beyond.ordersystem.common.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.host}")
    private String port;
    @Value("${spring.rabbitmq.host}")
    private String username;
    @Value("${spring.rabbitmq.host}")
    private String password;
    @Value("${spring.rabbitmq.host}")
    private String virtualHost;

//    스프링빈 생성을 통해 rebbitM Q에 자동으로 아래 변수명으로 큐가 생성
    @Bean
    public Queue stockQueue(){
        return new Queue("stockDecreaseQueue",true);
    }

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setHost(port);
        connectionFactory.setHost(username);
        connectionFactory.setHost(password);
        connectionFactory.setHost(virtualHost);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
