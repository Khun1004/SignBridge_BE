package com.signbridge.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

public class MyPageDto {

    @Getter
    @Builder
    public static class UserProfile {
        private String email;
        private String name;
        private String orgType;

        // 기관 공통
        private String officeName;
        private String orgCode;
        private String address;
        private String addressDetail;
        private String zonecode;

        // 개인용
        private String disabilityGrade;
        private String preferredSign;
    }

    // 출입국/경찰 등 리스트 아이템 공통 규격
    @Getter
    @Builder
    public static class CaseItem {
        private Long id;
        private String title; // 예: "비자 연장 신청", "사건 접수"
        private String status; // 예: "대기중", "완료"
        private String applicant; // 신청자/관련자 이름
        private LocalDateTime createdAt;
    }
}