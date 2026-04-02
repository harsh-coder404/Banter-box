package com.example.whatsapp.backend.chat;

import com.example.whatsapp.backend.chat.dto.MessageDto;
import com.example.whatsapp.backend.chat.dto.SendMessageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WsChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public WsChatController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload SendMessageRequest request) {
        MessageDto saved = messageService.save(request.senderId(), request.receiverId(), request.content());
        String chatKey = messageService.chatKey(request.senderId(), request.receiverId());
        messagingTemplate.convertAndSend("/topic/chat." + chatKey, saved);
    }
}

