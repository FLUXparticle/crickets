package com.example.crickets.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitReplyConfig {

    private static String replyQueueName(String hostname) {
        return "reply." + hostname + ".queue";
    }

    public static final String REPLY_EXCHANGE = "reply.exchange";

    public static String replyRoutingKey(String hostname) {
        return "reply." + hostname + ".routing.key";
    }

    @Bean
    Queue replyQueue(String hostname) {
        String queueName = replyQueueName(hostname);
        return new Queue(queueName);
    }

    @Bean
    Exchange replyExchange() {
        return new DirectExchange("reply.exchange");
    }

    @Bean
    Binding replyBinding(Queue replyQueue, Exchange replyExchange, String hostname) {
        return BindingBuilder.bind(replyQueue).to(replyExchange).with(replyRoutingKey(hostname)).noargs();
    }

}
