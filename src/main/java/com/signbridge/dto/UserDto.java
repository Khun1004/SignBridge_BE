package com.signbridge.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserDto {

    /* ── 회원가입 요청 ── */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignupRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        private String name;
        @NotBlank
        private String orgType;
        private String officeName;
        private String orgCode;
        private String address;
        private String addressDetail;
        private String zonecode;
        private String disabilityGrade;
        private String preferredSign;
    }

    /* ── 로그인 요청 ── */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    /* ── 로그인 응답 — 전체 프로필 포함 ── */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String message;
        private String email;
        private String name; // displayName
        private String orgType;
        private String realName; // 실제 이름
        private String officeName; // 기관명
        private String orgCode; // 기관 코드
        private String address; // 주소
        private String addressDetail; // 상세 주소
        private String zonecode; // 우편번호
        private String disabilityGrade; // 장애 등급
        private String preferredSign; // 수어와의 관계
        private LocalDateTime joinedAt; // 가입일 ← 추가
    }
}