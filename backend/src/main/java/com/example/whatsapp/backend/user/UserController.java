package com.example.whatsapp.backend.user;

import com.example.whatsapp.backend.user.dto.ContactDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/by-phone/{phoneNumber}")
    public ContactDto byPhone(@PathVariable String phoneNumber, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(NOT_FOUND, "Unauthorized");
        }

        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return new ContactDto(user.getId(), user.getName(), user.getPhoneNumber());
    }
}

