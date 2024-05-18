package com.example.crickets.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitSubscribeConfig {

    private static String subscribeQueueName(String hostname) {
        return "subscribe." + hostname + ".queue";
    }

    public static final String SUBSCRIBE_EXCHANGE = "subscribe.exchange";

    public static String subscribeRoutingKey(String hostname) {
        return "subscribe." + hostname + ".routing.key";
    }

    @Bean
    Queue subscribeQueue(String hostname) {
        return new Queue(subscribeQueueName(hostname));
    }

    @Bean
    Exchange subscribeExchange() {
        return new DirectExchange(SUBSCRIBE_EXCHANGE);
    }

    @Bean
    Binding subscribeBinding(Queue subscribeQueue, Exchange subscribeExchange, String hostname) {
        return BindingBuilder.bind(subscribeQueue).to(subscribeExchange).with(subscribeRoutingKey(hostname)).noargs();
    }

}
