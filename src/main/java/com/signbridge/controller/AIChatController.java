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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {

        @Value("${anthropic.api.key:}")
        private String anthropicApiKey;

        private final RestTemplate restTemplate = new RestTemplate();

        public record ChatRequest(
                        String userEmail,
                        List<Map<String, String>> messages) {
        }

        @PostMapping("/chat")
        public ResponseEntity<?> chat(@RequestBody ChatRequest req) {

                String key = anthropicApiKey == null ? "" : anthropicApiKey.trim();
                if (key.isBlank()) {
                        return ResponseEntity.status(503)
                                        .body(Map.of("error", "Anthropic API 키가 설정되지 않았습니다."));
                }

                try {
                        // ── 메시지 정리 (system 제외, user/assistant만) ────
                        List<Map<String, String>> msgs = new ArrayList<>();
                        if (req.messages() != null) {
                                for (Map<String, String> msg : req.messages()) {
                                        String role = msg.get("role");
                                        String content = msg.get("content");
                                        if (content != null && !content.isBlank()
                                                        && ("user".equals(role) || "assistant".equals(role))) {
                                                msgs.add(Map.of("role", role, "content", content));
                                        }
                                }
                        }
                        // 메시지가 없으면 기본 메시지 추가
                        if (msgs.isEmpty()) {
                                msgs.add(Map.of("role", "user", "content", "안녕하세요"));
                        }

                        System.out.println("[AI] Claude 호출, 메시지 수: " + msgs.size());

                        // ── Anthropic Claude API 호출 ──────────────────────
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("x-api-key", key);
                        headers.set("anthropic-version", "2023-06-01");

                        Map<String, Object> body = new HashMap<>();
                        body.put("model", "claude-haiku-4-5-20251001"); // 빠르고 저렴
                        body.put("max_tokens", 600);
                        body.put("system",
                                        "당신은 SignBridge의 AI 어시스턴트입니다. " +
                                                        "수어(한국수어), 청각장애, 의사소통 방법 전문가입니다. " +
                                                        "친절하고 쉽게 한국어로 답변해 주세요. " +
                                                        "수어 동작은 구체적으로 묘사해 주세요. " +
                                                        "3~5문장으로 간결하게 답변해 주세요.");
                        body.put("messages", msgs);

                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                        ResponseEntity<Map> response = restTemplate.postForEntity(
                                        "https://api.anthropic.com/v1/messages",
                                        entity,
                                        Map.class);

                        // ── Anthropic 응답을 OpenAI 형식으로 변환 ──────────
                        Map<String, Object> anthropicBody = response.getBody();
                        String replyText = "";
                        if (anthropicBody != null && anthropicBody.get("content") instanceof List<?> contentList
                                        && !contentList.isEmpty()) {
                                Object first = contentList.get(0);
                                if (first instanceof Map<?, ?> firstMap) {
                                        replyText = String.valueOf(firstMap.get("text"));
                                }
                        }

                        // OpenAI 형식으로 감싸서 반환 (앱 코드 변경 불필요)
                        Map<String, Object> result = Map.of(
                                        "choices", List.of(
                                                        Map.of("message", Map.of(
                                                                        "role", "assistant",
                                                                        "content", replyText))));

                        System.out.println("[AI] Claude 응답 완료");
                        return ResponseEntity.ok(result);

                } catch (org.springframework.web.client.HttpClientErrorException e) {
                        System.err.println("[AI] Anthropic HTTP 오류: " + e.getStatusCode());
                        System.err.println("[AI] 응답 본문: " + e.getResponseBodyAsString());
                        return ResponseEntity.status(e.getStatusCode())
                                        .body(Map.of("error", "Claude 오류 " + e.getStatusCode()));

                } catch (Exception e) {
                        System.err.println("[AI] 예외: " + e.getMessage());
                        e.printStackTrace();
                        return ResponseEntity.status(500)
                                        .body(Map.of("error", e.getMessage()));
                }
        }
}