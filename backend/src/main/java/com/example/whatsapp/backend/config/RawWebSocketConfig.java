package com.example.whatsapp.backend.config;

import com.example.whatsapp.backend.chat.RawChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final RawChatWebSocketHandler rawChatWebSocketHandler;

    public RawWebSocketConfig(RawChatWebSocketHandler rawChatWebSocketHandler) {
        this.rawChatWebSocketHandler = rawChatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rawChatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}

