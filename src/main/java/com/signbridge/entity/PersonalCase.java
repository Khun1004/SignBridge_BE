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
 * personal_cases 테이블
 * 개인 사용자의 대화 기록 등록 케이스
 */
@Entity
@Table(name = "personal_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 등록자 이름 */
    @Column(name = "name", length = 100)
    private String name;

    /** 로그인 이메일 (nullable — 비로그인 허용) */
    @Column(name = "user_email", length = 255)
    private String userEmail;

    /** 메모 */
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    /** 대표 영상 ID */
    @Column(name = "video_id")
    private Long videoId;

    /** 추가 영상 ID 목록 (JSON 문자열로 저장) */
    @Column(name = "extra_video_ids", length = 500)
    private String extraVideoIds;

    /** 대화 세션 ID (conversations 테이블과 연결) */
    @Column(name = "session_id", length = 64)
    private String sessionId;

    /** 총 메시지 수 */
    @Column(name = "message_count")
    private Integer messageCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}