package com.signbridge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.signbridge.converter.StringListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "police_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoliceCase {

    @Id
    @Column(name = "case_id", length = 50)
    private String caseId; // POL-2026-0001

    @Column(name = "user_email", length = 100)
    private String userEmail; // 담당 경찰서 계정 이메일

    // ── 대상자(당사자) 정보 ──
    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name = "subject_birth", length = 20)
    private String subjectBirth;

    @Column(name = "subject_disability", length = 50)
    private String subjectDisability;

    @Column(name = "subject_nationality", length = 50)
    private String subjectNationality;

    @Column(name = "subject_phone", length = 20)
    private String subjectPhone;

    @Column(name = "subject_role", length = 20)
    private String subjectRole; // 피해자 / 참고인 / 피의자 등

    // ── 담당 경찰관 정보 ──
    @Column(name = "officer_name", length = 50)
    private String officerName;

    @Column(name = "officer_badge", length = 30)
    private String officerBadge;

    @Column(name = "officer_rank", length = 20)
    private String officerRank; // 순경 / 경장 / 경위 등

    @Column(name = "officer_department", length = 100)
    private String officerDepartment; // 형사과 1팀

    @Column(name = "officer_station", length = 100)
    private String officerStation; // 서울 강남경찰서

    // ── 사건 정보 ──
    @Column(name = "case_type", length = 50)
    private String caseType; // 피해신고 / 참고인조사 / 분실신고 등

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "duration", length = 20)
    private String duration;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_type", length = 10)
    private String statusType; // ok / warn / danger

    @Column(name = "is_flagged")
    private boolean flagged;

    // ── 대화 내용 ──
    @Convert(converter = StringListConverter.class)
    @Column(name = "signs", columnDefinition = "TEXT")
    private List<String> signs;

    @Convert(converter = StringListConverter.class)
    @Column(name = "voice", columnDefinition = "TEXT")
    private List<String> voice;

    // ── 영상 ──
    @Column(name = "video_id")
    private Long videoId;

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
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}