package com.example.whatsapp.backend.chat;

import com.example.whatsapp.backend.chat.dto.MessageDto;
import com.example.whatsapp.backend.chat.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public MessageDto send(@Valid @RequestBody SendMessageRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!userId.equals(request.senderId())) {
            throw new ResponseStatusException(FORBIDDEN, "Sender mismatch");
        }
        return messageService.save(request.senderId(), request.receiverId(), request.content());
    }

    @GetMapping("/{otherUserId}")
    public List<MessageDto> history(@PathVariable Long otherUserId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return messageService.history(userId, otherUserId);
    }
}

