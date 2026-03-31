package com.signbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignResultDto {
    private String gesture; // 인식된 수어 동작 이름 (예: 안녕하세요)
    private String emoji; // 이모지 (예: 👋)
    private String meaning; // 동작 의미
    private String pose; // pose 키 (예: wave)
    private double score; // 인식 신뢰도
    private String timestamp; // 인식 시각
    private String userEmail; // 사용자 이메일 (저장 시 연결)
}