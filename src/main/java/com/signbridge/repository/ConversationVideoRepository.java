package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signbridge.entity.ConversationVideo;

public interface ConversationVideoRepository extends JpaRepository<ConversationVideo, Long> {
    List<ConversationVideo> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<ConversationVideo> findByImmigrationCaseId(String immigrationCaseId);
}