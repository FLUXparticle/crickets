package com.example.crickets.controller;

import com.example.crickets.service.*;
import org.slf4j.*;
import org.springframework.context.event.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.messaging.*;

import java.security.*;

@Controller
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        Principal principal = event.getUser();
        String name = principal.getName();

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        LOGGER.info("Client connected: {}", name);

        chatService.enterRoom(sessionId, name);
    }

    @MessageMapping("/send-partial-message")
    public void sendPartialMessage(String message, Authentication authentication, SimpMessageHeaderAccessor headerAccessor) {
        String name = authentication.getName();
        String sessionId = headerAccessor.getSessionId();
        LOGGER.info("Partial message from user '{}' in session '{}' = {}", name, sessionId, message);
        chatService.sendChatMessage(sessionId, name, message, true);
    }

    @MessageMapping("/send-message")
    public void sendMessage(String message, Authentication authentication, SimpMessageHeaderAccessor headerAccessor) {
        String name = authentication.getName();
        String sessionId = headerAccessor.getSessionId();
        LOGGER.info("Message from user '{}' in session '{}' = {}", name, sessionId, message);
        chatService.sendChatMessage(sessionId, name, message, false);
    }

}
