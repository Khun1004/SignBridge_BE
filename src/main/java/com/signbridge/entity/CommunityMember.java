package com.signbridge.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_members")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommunityMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_email")
    private String userEmail;

    // Unique public ID for SignBridge chat — e.g. "tsolmon"
    // Shown as @tsolmon on community profile
    @Column(name = "chat_id")
    private String chatId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(columnDefinition = "TEXT")
    private String experience;

    private String speciality;

    @Column(name = "contact_type")
    private String contactType;   // signbridge, phone, email

    @Column(name = "contact_value")
    private String contactValue;

    @Column(name = "public_profile")
    private Boolean publicProfile = true;

    @Column(name = "cert_file_names")
    private String certFileNames;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}