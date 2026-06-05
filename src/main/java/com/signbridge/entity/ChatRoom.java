package com.signbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatRoom {

    @Id
    @Column(name = "room_id")
    private String roomId;          // e.g. "room_1717430400000"

    @Column(nullable = false)
    private String name;

    private String sub;             // subtitle / role

    private String avatar;          // single emoji char

    @Column(name = "is_group")
    private Boolean isGroup = false;

    @Column(name = "is_official")
    private Boolean isOfficial = false;

    @Column(name = "last_msg")
    private String lastMsg;

    @Column(name = "last_at")
    private LocalDateTime lastAt;

    // Comma-separated emails of participants (for 1:1 rooms)
    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        lastAt    = LocalDateTime.now();
    }
}