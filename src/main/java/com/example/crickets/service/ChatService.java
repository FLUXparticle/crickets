package com.example.crickets.service;

import org.slf4j.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.messaging.simp.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;

import static com.example.crickets.configuration.RabbitChatConfig.*;

@Service
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private int cntMessages = 0;

    private final Map<String, Integer> lines = new HashMap<>();

    private final RabbitTemplate rabbitTemplate;

    private final SimpMessagingTemplate messagingTemplate;

    private final String hostname;

    public ChatService(RabbitTemplate rabbitTemplate, SimpMessagingTemplate messagingTemplate, String hostname) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.hostname = hostname;
    }

    public void enterRoom(String sessionId, String name) {
        String textMessage = name + " hat den Raum betreten.";
        sendChatMessage(sessionId, null, textMessage, false);
    }

    public void sendChatMessage(String sessionId, String name, String textMessage, boolean isPartial) {
        ChatMessage chatMessage = new ChatMessage(joinSessionId(sessionId), name, textMessage, isPartial);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "", chatMessage);
    }

    @RabbitListener(queues = "#{chatQueue.name}")
    public void handleMessage(ChatMessage chatMessage) {
        // Verarbeiten der eingehenden Nachricht
        LOGGER.info("Empfangene Nachricht: {}", chatMessage);

        String sessionId = chatMessage.sessionId;
        String textMessage = joinMessage(chatMessage.name, chatMessage.textMessage);
        int line = lines.computeIfAbsent(sessionId, key -> newLine());
        if (chatMessage.isPartial) {
            sendLine(line, textMessage + "_");
        } else {
            sendLine(line, textMessage);
            lines.remove(sessionId);
        }
    }

    private int newLine() {
        return cntMessages++;
    }

    private void sendLine(int line, String textMessage) {
        LineUpdate lineUpdate = new LineUpdate(line, textMessage);
        messagingTemplate.convertAndSend("/topic/messages", lineUpdate);
    }

    private String joinSessionId(String sessionId) {
        return hostname + "/" + sessionId;
    }

    private static String joinMessage(String name, String text) {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
            sb.append(": ");
        }
        sb.append(text);
        return sb.toString();
    }

    public static final class LineUpdate {
        private final int line;
        private final String message;

        public LineUpdate(int line, String message) {
            this.line = line;
            this.message = message;
        }
        public int getLine() {
            return line;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "LineUpdate[" +
                    "line=" + line + ", " +
                    "message=" + message + ']';
        }
    }

    public static final class ChatMessage implements Serializable {
        private final String sessionId;
        private final String name;
        private final String textMessage;
        private final boolean isPartial;

        public ChatMessage(String sessionId, String name, String textMessage, boolean isPartial) {
            this.sessionId = sessionId;
            this.name = name;
            this.textMessage = textMessage;
            this.isPartial = isPartial;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getName() {
            return name;
        }

        public String getTextMessage() {
            return textMessage;
        }

        public boolean isPartial() {
            return isPartial;
        }

        @Override
        public String toString() {
            return "ChatMessage[" +
                    "sessionId=" + sessionId + ", " +
                    "name=" + name + ", " +
                    "textMessage=" + textMessage + ", " +
                    "isPartial=" + isPartial + ']';
        }
    }

}
