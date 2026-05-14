package com.signbridge.dto;

import lombok.*;
import java.util.List;

public class ImmigrationCaseDto {

    /** 케이스 저장 요청 (RegisterImmigration.jsx → POST /api/immigration/cases) */
    @Getter
    @NoArgsConstructor
    public static class SaveRequest {
        private String       userEmail;
        private String       officerName;
        private String       applicantName;
        private String       caseNumber;
        private String       purpose;
        private Long         videoId;
        private List<Long>   extraVideoIds;
        private List<String> signs;
        private List<String> voice;

        // 빈 문자열 → null 정규화
        public String getSafeEmail() {
            return (userEmail != null && !userEmail.isBlank()) ? userEmail.trim() : null;
        }
    }

    /** 케이스 저장 응답 */
    @Getter
    @Builder
    public static class SaveResponse {
        private String caseId;
        private String message;
    }

    /** 케이스 목록 아이템 (GET /api/immigration/cases 응답) */
    @Getter
    @Builder
    public static class CaseItem {
        private String       id;
        private ApplicantInfo applicant;
        private OfficerInfo   officer;
        private String       purpose;
        private String       caseNumber;
        private String       date;
        private String       time;
        private String       location;
        private String       duration;
        private String       status;
        private String       statusType;
        private boolean      flagged;
        private List<String> signs;
        private List<String> voice;
        private Long         videoId;
        private List<Long>   videoIds;
    }

    @Getter
    @Builder
    public static class ApplicantInfo {
        private String name;
        private String birth;
        private String disability;
        private String nationality;
        private String phone;
        private String avatar;
    }

    @Getter
    @Builder
    public static class OfficerInfo {
        private String name;
        private String badge;
        private String department;
        private String position;
        private String avatar;
    }
}