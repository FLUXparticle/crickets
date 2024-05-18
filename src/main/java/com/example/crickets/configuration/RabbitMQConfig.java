package com.example.crickets.configuration;

import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.*;

import java.util.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public SimpleMessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("java.lang.*", "java.util.*", "com.example.crickets.*"));
        return converter;
    }

}
