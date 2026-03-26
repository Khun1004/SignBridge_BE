package com.signbridge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class SignHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gesture; // 인식된 주요 단어

    @Column(length = 500)
    private String sentence; // Claude API로 생성된 전체 문장

    private Double confidence; // 모델의 예측 신뢰도

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public SignHistory(String gesture, String sentence, Double confidence) {
        this.gesture = gesture;
        this.sentence = sentence;
        this.confidence = confidence;
    }
}
