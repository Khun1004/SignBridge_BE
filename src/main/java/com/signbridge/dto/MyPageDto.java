package com.signbridge.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

public class MyPageDto {

    @Getter
    @Builder
    public static class UserProfile {
        private String email;
        private String name;
        private String orgType;
        private String officeName;
        private String preferredSign; // 개인용
        private String disabilityGrade; // 개인용
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