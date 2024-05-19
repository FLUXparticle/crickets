package com.example.crickets.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitChatConfig {

    private static String chatQueueName(String hostname) {
        return "chat." + hostname + ".queue";
    }

    public static final String CHAT_EXCHANGE = "chat.exchange";

    @Bean
    Queue chatQueue(String hostname) {
        return new Queue(chatQueueName(hostname));
    }

    @Bean
    FanoutExchange chatExchange() {
        return new FanoutExchange(CHAT_EXCHANGE);
    }

    @Bean
    Binding chatBinding(Queue chatQueue, FanoutExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange);
    }

}
