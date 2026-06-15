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
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "is_image")
    private Boolean isImage = false;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reply_to_name")
    private String replyToName;

    @Column(name = "reply_to_text")
    private String replyToText;

    @Column(name = "forwarded_from")
    private String forwardedFrom;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    // ✅ 이 채팅방을 삭제한 사용자 이메일 목록 (쉼표 구분)
    // 예: "san@email.com" → San에게만 안 보임, Khun에게는 그대로
    // 커뮤니티 채팅하기로 재입장 시 해당 email 제거하면 다시 보임
    @Column(name = "deleted_for", length = 1000)
    private String deletedFor;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        sentAt = LocalDateTime.now();
    }
}