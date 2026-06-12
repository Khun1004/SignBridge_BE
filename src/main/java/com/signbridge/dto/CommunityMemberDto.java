package com.signbridge.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CommunityMemberDto {

    /* ── 등록 / 수정 요청 ── */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        private String name;
        private String userEmail;
        private String role;
        private String region;
        private String intro;
        private String experience;
        private String speciality;
        private String contactType;
        private String contactValue;
        private Boolean publicProfile;
        private List<String> certFileNames;
        private String chatId;
    }

    /* ── 응답 ── */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long   id;
        private String name;
        private String userEmail;
        private String role;
        private String region;
        private String intro;
        private String experience;
        private String speciality;
        private String contactType;
        private String contactValue;
        private Boolean publicProfile;
        private List<String> certFileNames;
        private String avatar;           // name 첫 글자
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String chatId;
    }
}