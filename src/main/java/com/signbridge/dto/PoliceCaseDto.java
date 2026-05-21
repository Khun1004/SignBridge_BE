package com.signbridge.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PoliceCaseDto {

    /** 케이스 저장 요청 */
    @Getter
    @NoArgsConstructor
    public static class SaveRequest {
        private String userEmail;
        private String officerName;
        private String officerBadge;
        private String officerRank;
        private String officerDepartment;
        private String officerStation;
        private String subjectName;
        private String subjectRole;
        private String caseType;
        private String caseNumber;
        private Long videoId;
        private List<Long> extraVideoIds;
        private List<String> signs;
        private List<String> voice;

        public String getSafeEmail() {
            return (userEmail != null && !userEmail.isBlank()) ? userEmail.trim() : null;
        }
    }

    /** 저장 응답 */
    @Getter
    @Builder
    public static class SaveResponse {
        private String caseId;
        private String message;
    }

    /** 목록 아이템 */
    @Getter
    @Builder
    public static class CaseItem {
        private String id;
        private SubjectInfo subject;
        private OfficerInfo officer;
        private String caseType;
        private String caseNumber;
        private String date;
        private String time;
        private String location;
        private String duration;
        private String status;
        private String statusType;
        private boolean flagged;
        private List<String> signs;
        private List<String> voice;
        private Long videoId;
        private List<Long> videoIds;
    }

    @Getter
    @Builder
    public static class SubjectInfo {
        private String name;
        private String birth;
        private String disability;
        private String nationality;
        private String phone;
        private String role;
        private String avatar;
    }

    @Getter
    @Builder
    public static class OfficerInfo {
        private String name;
        private String badge;
        private String rank;
        private String department;
        private String station;
        private String avatar;
    }
}