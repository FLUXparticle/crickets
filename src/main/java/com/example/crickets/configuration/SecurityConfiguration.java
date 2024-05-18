package com.example.crickets.configuration;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.*;
import org.springframework.security.web.*;

import java.util.*;
import java.util.stream.*;

import static org.springframework.security.config.Customizer.*;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .formLogin(withDefaults())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = Stream.of("admin", "helpdesk", "employee", "manager")
                .map(username -> User.withDefaultPasswordEncoder()
                        .username(username)
                        .password("Secret123")
                        .build())
                .toList();

        return new InMemoryUserDetailsManager(users);
    }

}
