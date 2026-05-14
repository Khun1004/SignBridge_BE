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

/**
 * 대화 세션의 녹화 영상 메타데이터를 저장하는 엔티티.
 * 실제 파일은 서버 파일시스템(또는 S3)에 저장되고,
 * 이 테이블은 파일 경로와 연결 정보만 관리한다.
 */
@Entity
@Table(name = "conversation_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 파일 저장 경로 (예: /videos/recording_1234567890.webm) */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** 원본 파일명 */
    @Column(name = "original_filename", length = 200)
    private String originalFilename;

    /** MIME 타입 (video/webm, video/mp4 등) */
    @Column(name = "mime_type", length = 50)
    private String mimeType;

    /** 파일 크기 (bytes) */
    @Column(name = "file_size")
    private Long fileSize;

    /** 업로드한 사용자 이메일 */
    @Column(name = "user_email", length = 100)
    private String userEmail;

    /** 연결된 출입국 케이스 ID (nullable — 케이스 등록 전엔 null) */
    @Column(name = "immigration_case_id", length = 50)
    private String immigrationCaseId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}