package com.example.whatsapp.backend.user;

import com.example.whatsapp.backend.user.dto.AddContactRequest;
import com.example.whatsapp.backend.user.dto.ContactDto;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<ContactDto> contacts(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return contactService.getContacts(userId);
    }

    @PostMapping
    public ContactDto addContact(@Valid @RequestBody AddContactRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return contactService.addContactByPhone(userId, request.contactPhoneNumber());
    }
}

