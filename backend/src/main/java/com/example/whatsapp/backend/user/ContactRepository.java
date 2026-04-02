package com.example.whatsapp.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<ContactEntity, Long> {
    List<ContactEntity> findAllByOwnerUserId(Long ownerUserId);
    boolean existsByOwnerUserIdAndContactUserId(Long ownerUserId, Long contactUserId);
}

