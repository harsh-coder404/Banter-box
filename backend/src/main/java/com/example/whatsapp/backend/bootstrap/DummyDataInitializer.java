package com.example.whatsapp.backend.bootstrap;

import com.example.whatsapp.backend.user.ContactEntity;
import com.example.whatsapp.backend.user.ContactRepository;
import com.example.whatsapp.backend.user.UserEntity;
import com.example.whatsapp.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DummyDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;

    public DummyDataInitializer(
            UserRepository userRepository,
            ContactRepository contactRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        UserEntity first = ensureUser("Golu", "9890989098");
        UserEntity second = ensureUser("Monu", "6262626262");
        UserEntity third = ensureUser("Sonu", "8787878787");

        ensureContact(first.getId(), second.getId());
        ensureContact(second.getId(), first.getId());

        ensureContact(first.getId(), third.getId());
        ensureContact(third.getId(), first.getId());

        ensureContact(second.getId(), third.getId());
        ensureContact(third.getId(), second.getId());
    }

    private UserEntity ensureUser(String name, String phone) {
        UserEntity user = userRepository.findByPhoneNumber(phone).orElseGet(UserEntity::new);
        user.setName(name);
        user.setPhoneNumber(phone);
        user.setPasswordHash(passwordEncoder.encode("0000"));
        return userRepository.save(user);
    }

    private void ensureContact(Long ownerId, Long contactId) {
        if (!contactRepository.existsByOwnerUserIdAndContactUserId(ownerId, contactId)) {
            ContactEntity entity = new ContactEntity();
            entity.setOwnerUserId(ownerId);
            entity.setContactUserId(contactId);
            contactRepository.save(entity);
        }
    }
}



