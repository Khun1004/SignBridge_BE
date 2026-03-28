package com.signbridge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UserDto {

    @Getter
    @Setter
    public static class SignupRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6)
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

    @Getter
    @Setter
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    @Builder
    public static class LoginResponse {
        private String name;
        private String orgType;
        private String email;
        private String message;
    }
}
