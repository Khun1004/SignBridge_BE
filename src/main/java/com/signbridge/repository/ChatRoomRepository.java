package com.signbridge.repository;

import com.signbridge.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    // Find rooms where this email is a participant
    List<ChatRoom> findByParticipantsContaining(String email);

    // Find 1:1 room between two users (both emails must appear)
    Optional<ChatRoom> findByParticipantsContainingAndParticipantsContaining(
            String email1, String email2);
}