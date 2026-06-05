package com.signbridge.repository;

import com.signbridge.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Load all messages for a room, oldest first
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(String roomId);

    // Load last N messages (for initial load when opening a room)
    List<ChatMessage> findTop50ByRoomIdOrderBySentAtDesc(String roomId);
}