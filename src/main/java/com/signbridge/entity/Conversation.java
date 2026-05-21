package com.signbridge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    // ── length 100 → 255 로 수정 (DB와 일치) ──
    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "msg_type", length = 20)
    private String msgType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "pose", length = 50)
    private String pose;

    @Column(name = "sent_at", length = 30)
    private String sentAt;

    @Column(name = "place", length = 30)
    private String place;

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}