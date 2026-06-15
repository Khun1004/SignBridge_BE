package com.signbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.signbridge.entity.ChatRoom;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    List<ChatRoom> findByParticipantsContaining(String email);

    Optional<ChatRoom> findByParticipantsContainingAndParticipantsContaining(
            String email1, String email2);

    List<ChatRoom> findByIsOfficialTrue();

    List<ChatRoom> findByIsGroupTrue();

    // ✅ 추가
    Optional<ChatRoom> findByRoomId(String roomId);
}