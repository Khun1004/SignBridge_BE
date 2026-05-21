package com.signbridge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubtitleController {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * POST /api/subtitle
     * 수어 토큰 배열 → 자연스러운 한국어 문장 생성
     * body: { words: ["안녕하세요", "만나서 반갑습니다", "좋아합니다"], place: "personal" }
     */
    @PostMapping("/subtitle")
    public ResponseEntity<?> buildSubtitle(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> words = (List<String>) body.get("words");
            String place = body.getOrDefault("place", "personal").toString();

            if (words == null || words.isEmpty()) {
                return ResponseEntity.ok(Map.of("sentence", ""));
            }

            // API 키 없으면 로컬 변환
            if (apiKey == null || apiKey.isBlank()) {
                return ResponseEntity.ok(Map.of("sentence", localConvert(words)));
            }

            // Claude API 호출
            String prompt = buildPrompt(words, place);
            String sentence = callClaude(prompt);
            return ResponseEntity.ok(Map.of("sentence", sentence));

        } catch (Exception e) {
            log.error("[Subtitle] 오류: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("sentence", String.join(" ", (List<String>) body.get("words"))));
        }
    }

    // ── Claude API 호출 ───────────────────────────────────────
    private String callClaude(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-haiku-4-5-20251001");
        requestBody.put("max_tokens", 200);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages", request, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        return content.get(0).get("text").toString().trim();
    }

    // ── 프롬프트 생성 ─────────────────────────────────────────
    private String buildPrompt(List<String> words, String place) {
        String placeLabel = switch (place) {
            case "immigration" -> "출입국 관련";
            case "police" -> "경찰서 관련";
            case "hospital" -> "병원 관련";
            default -> "일상";
        };
        return String.format(
                "다음은 청각장애인이 수어로 표현한 핵심 단어들입니다: %s\n" +
                        "상황: %s\n" +
                        "이 단어들을 자연스러운 한국어 문장으로 만들어주세요.\n" +
                        "규칙:\n" +
                        "- 주어(저는, 제가 등)와 조사를 자연스럽게 추가하세요\n" +
                        "- 문장은 1~2개로 간결하게\n" +
                        "- 문장만 출력하고 설명은 하지 마세요",
                String.join(", ", words), placeLabel);
    }

    // ── 로컬 변환 (API 키 없을 때) ────────────────────────────
    private String localConvert(List<String> words) {
        Map<String, String> map = Map.of(
                "안녕하세요", "안녕하세요.",
                "만나서 반갑습니다", "만나서 반갑습니다.",
                "반갑습니다", "반갑습니다.",
                "좋아합니다", "저는 당신을 좋아합니다.",
                "좋아요", "좋아요.",
                "고맙습니다", "고맙습니다.",
                "감사합니다", "감사합니다.",
                "미안합니다", "미안합니다.",
                "사랑합니다", "사랑합니다.");
        List<String> result = new ArrayList<>();
        for (String w : words) {
            result.add(map.getOrDefault(w, w));
        }
        return String.join(" ", result);
    }
}