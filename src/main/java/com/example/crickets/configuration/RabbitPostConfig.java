package com.example.crickets.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitPostConfig {

    private static String postQueueName(String hostname) {
        return "post." + hostname + ".queue";
    }

    public static final String POST_EXCHANGE = "post.exchange";

    public static String postRoutingKey(String hostname) {
        return "post." + hostname + ".routing.key";
    }

    @Bean
    Queue postQueue(String hostname) {
        return new Queue(postQueueName(hostname));
    }

    @Bean
    Exchange postExchange() {
        return new DirectExchange(POST_EXCHANGE);
    }

    @Bean
    Binding postBinding(Queue postQueue, Exchange postExchange, String hostname) {
        return BindingBuilder.bind(postQueue).to(postExchange).with(postRoutingKey(hostname)).noargs();
    }

}
