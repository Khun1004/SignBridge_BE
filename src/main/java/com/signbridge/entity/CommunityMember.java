package com.signbridge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "community_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_email")
    private String userEmail;

    @Column(nullable = false)
    private String role; // 수어 선생님, 수어 통역사 등

    @Column(nullable = false)
    private String region; // 서울, 부산 등

    @Column(columnDefinition = "TEXT")
    private String intro; // 자기소개

    @Column(columnDefinition = "TEXT")
    private String experience; // 경력/활동 이력

    private String speciality; // 전문 분야 (콤마 구분)

    @Column(name = "contact_type")
    private String contactType; // chat, phone, email

    @Column(name = "contact_value")
    private String contactValue;

    @Column(name = "public_profile")
    private Boolean publicProfile = true;

    @Column(name = "cert_file_names")
    private String certFileNames; // 자격증 파일명 (콤마 구분)

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