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

@Entity
@Table(name = "immigration_cases")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImmigrationCase {

    @Id
    @Column(name = "case_id", length = 50)
    private String caseId; // 예: IMM-2025-001

    @Column(name = "user_email", length = 100)
    private String userEmail; // 담당 기관 계정 이메일

    // ── 신청자(민원인) 정보 ──
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

    // ── 담당 심사관 정보 ──
    @Column(name = "officer_name", length = 50)
    private String officerName;

    @Column(name = "officer_badge", length = 30)
    private String officerBadge;

    @Column(name = "officer_department", length = 100)
    private String officerDepartment;

    @Column(name = "officer_position", length = 50)
    private String officerPosition;

    // ── 사건 세부 정보 ──
    @Column(name = "purpose", length = 100)
    private String purpose;

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

    // signs/voice: JSON 문자열로 직렬화하여 TEXT 컬럼에 저장
    @Convert(converter = StringListConverter.class)
    @Column(name = "signs", columnDefinition = "TEXT")
    private List<String> signs;

    @Convert(converter = StringListConverter.class)
    @Column(name = "voice", columnDefinition = "TEXT")
    private List<String> voice;

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