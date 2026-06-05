package com.signbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which room this message belongs to (matches roomId from your React frontend)
    @Column(name = "room_id", nullable = false)
    private String roomId;

    // Sender's email — matches myEmail prop in your React ChatRoom
    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String text;

    // For image/file messages
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")       // store S3/local path, not base64
    private String fileUrl;

    @Column(name = "is_image")
    private Boolean isImage = false;

    // The message being replied to (null if not a reply)
    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reply_to_name")
    private String replyToName;

    @Column(name = "reply_to_text")
    private String replyToText;

    // For forwarded messages
    @Column(name = "forwarded_from")
    private String forwardedFrom;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        sentAt = LocalDateTime.now();
    }
}