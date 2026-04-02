package com.example.whatsapp.backend.user;

import com.example.whatsapp.backend.user.dto.ContactDto;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public List<ContactDto> getContacts(Long ownerUserId) {
        return contactRepository.findAllByOwnerUserId(ownerUserId).stream()
                .map(ContactEntity::getContactUserId)
                .map(userRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(u -> new ContactDto(u.getId(), u.getName(), u.getPhoneNumber()))
                .toList();
    }

    public ContactDto addContactByPhone(Long ownerUserId, String contactPhoneNumber) {
        UserEntity contactUser = userRepository.findByPhoneNumber(contactPhoneNumber)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contact user not found"));

        if (ownerUserId.equals(contactUser.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot add yourself as contact");
        }

        boolean exists = contactRepository.existsByOwnerUserIdAndContactUserId(ownerUserId, contactUser.getId());
        if (!exists) {
            ContactEntity entity = new ContactEntity();
            entity.setOwnerUserId(ownerUserId);
            entity.setContactUserId(contactUser.getId());
            contactRepository.save(entity);
        }

        return new ContactDto(contactUser.getId(), contactUser.getName(), contactUser.getPhoneNumber());
    }
}

