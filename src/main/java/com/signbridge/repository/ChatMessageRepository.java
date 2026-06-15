package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.signbridge.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(String roomId);

    List<ChatMessage> findTop50ByRoomIdOrderBySentAtDesc(String roomId);

    @Transactional
    void deleteAllByRoomId(String roomId);
}