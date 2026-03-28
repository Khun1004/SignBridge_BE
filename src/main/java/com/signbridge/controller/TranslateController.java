package com.signbridge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TranslateController {

    // ── 수어 단어 배열 → 자연스러운 문장 (Mock) ──
    @PostMapping("/subtitle")
    public ResponseEntity<?> buildSubtitle(@RequestBody SubtitleRequest req) {
        if (req.getWords() == null || req.getWords().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "words가 비어있습니다."));
        }

        // Mock: 단어를 이어붙여 문장 생성 (나중에 AI 연동 시 교체)
        String sentence = String.join(" ", req.getWords()) + ".";

        return ResponseEntity.ok(Map.of("sentence", sentence));
    }

    // ── 텍스트 → 수어 가이드 (Mock) ──
    @PostMapping("/sign-guide")
    public ResponseEntity<?> getSignGuide(@RequestBody SignGuideRequest req) {
        if (req.getText() == null || req.getText().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text가 비어있습니다."));
        }

        // Mock 응답 (나중에 AI 연동 시 교체)
        return ResponseEntity.ok(Map.of(
                "text", req.getText(),
                "steps", List.of(
                        Map.of("order", 1, "word", req.getText(), "description", "준비 중인 수어 가이드입니다.", "pose", "wave"))));
    }

    // ── 요청 DTO ──
    static class SubtitleRequest {
        private List<String> words;

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }
    }

    static class SignGuideRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}