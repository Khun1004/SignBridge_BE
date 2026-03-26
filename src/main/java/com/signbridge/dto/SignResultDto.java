package com.signbridge.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignResultDto {
    private String gesture; // 인식된 단어 (예: "안녕하세요")
    private Double confidence; // 신뢰도 (예: 0.92)
    private String sentence; // 완성된 문장
    private List<String> tokens; // 인식된 단어 리스트
}
