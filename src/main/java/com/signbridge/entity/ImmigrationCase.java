package com.signbridge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.signbridge.converter.StringListConverter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "immigration_cases")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ImmigrationCase {

    @Id
    @Column(name = "case_id", length = 50)
    private String caseId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    // ── 신청자 정보 ──
    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Column(name = "applicant_birth", length = 20)
    private String applicantBirth;

    @Column(name = "applicant_disability", length = 50)
    private String applicantDisability;

    @Column(name = "applicant_nationality", length = 50)
    private String applicantNationality;

    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    // ── 담당자 정보 ──
    @Column(name = "officer_name", length = 50)
    private String officerName;

    @Column(name = "officer_badge", length = 30)
    private String officerBadge;

    @Column(name = "officer_department", length = 100)
    private String officerDepartment;

    @Column(name = "officer_position", length = 50)
    private String officerPosition;

    // ── 사건 정보 ──
    @Column(name = "purpose", length = 100)
    private String purpose;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "duration", length = 20)
    private String duration;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_type", length = 10)
    private String statusType;

    @Column(name = "is_flagged")
    private boolean flagged;

    // ── 대화 내용 ──
    @Convert(converter = StringListConverter.class)
    @Column(name = "signs", columnDefinition = "TEXT")
    private List<String> signs;

    @Convert(converter = StringListConverter.class)
    @Column(name = "voice", columnDefinition = "TEXT")
    private List<String> voice;

    // ── 영상 ──────────────────────────────────────────────────
    /** 대표 영상 ID (conversation_videos 테이블 참조) */
    @Column(name = "video_id")
    private Long videoId;

    /** 추가 영상 ID 목록 (JSON 문자열) */
    @Column(name = "extra_video_ids", length = 500)
    private String extraVideoIds;

    // ── 날짜/시간 ──
    @Column(name = "case_date")
    private LocalDate caseDate;

    @Column(name = "case_time")
    private LocalTime caseTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}