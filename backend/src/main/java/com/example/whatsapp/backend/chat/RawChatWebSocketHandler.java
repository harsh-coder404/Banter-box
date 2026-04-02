package com.example.whatsapp.backend.chat;

import com.example.whatsapp.backend.chat.dto.MessageDto;
import com.example.whatsapp.backend.chat.dto.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RawChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public RawChatWebSocketHandler(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session.getUri());
        if (userId == null) {
            try {
                session.close(CloseStatus.BAD_DATA);
            } catch (IOException ignored) {
            }
            return;
        }

        session.getAttributes().put("userId", userId);
        sessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SendMessageRequest request = objectMapper.readValue(message.getPayload(), SendMessageRequest.class);
        MessageDto saved = messageService.save(request.senderId(), request.receiverId(), request.content());
        String payload = objectMapper.writeValueAsString(saved);

        sendToUser(request.senderId(), payload);
        sendToUser(request.receiverId(), payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userIdObj = session.getAttributes().get("userId");
        if (!(userIdObj instanceof Long userId)) {
            return;
        }

        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }

        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            sessions.remove(userId);
        }
    }

    private void sendToUser(Long userId, String payload) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }

        for (WebSocketSession ws : userSessions) {
            if (ws.isOpen()) {
                try {
                    ws.sendMessage(new TextMessage(payload));
                } catch (IOException ignored) {
                }
            }
        }
    }

    private Long extractUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                try {
                    return Long.parseLong(keyValue[1]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}

