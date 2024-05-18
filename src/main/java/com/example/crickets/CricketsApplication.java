package com.example.crickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;

import java.net.*;

@SpringBootApplication
public class CricketsApplication {

    @Bean
    public String hostname() throws UnknownHostException {
        String hostname = InetAddress.getLocalHost().getHostName();
        if (hostname.contains(".")) {
            return "localhost";
        }
        return hostname;
    }

    public static void main(String[] args) {
        SpringApplication.run(CricketsApplication.class, args);
    }

}
