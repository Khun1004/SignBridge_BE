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
 * conversations 테이블
 * — 대화 한 건 = 한 메시지 행 (한 세션의 메시지들을 여러 행으로 저장)
 *
 * DB 컬럼: id, user_email, msg_type, content, pose, created_at
 * (이미지 1의 conversations 테이블과 동일한 구조)
 */
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

    /** 세션 구분용 UUID (한 대화 세션의 모든 메시지가 같은 session_id를 가짐) */
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    /** 'sign' | 'voice' */
    @Column(name = "msg_type", length = 20)
    private String msgType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 수어 포즈 이름 (nullable) */
    @Column(name = "pose", length = 50)
    private String pose;

    /** 메시지 전송 시각 문자열 (예: "오전 12:27") */
    @Column(name = "sent_at", length = 30)
    private String sentAt;

    /** 장소 구분 (immigration | police | personal 등) */
    @Column(name = "place", length = 30)
    private String place;

    /** 연결된 녹화 영상 ID (nullable) */
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