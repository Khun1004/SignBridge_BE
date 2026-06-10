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

        @Value("${google.gemini.api.key:}")
        private String geminiApiKey;

        private final RestTemplate restTemplate = new RestTemplate();

        public record ChatRequest(
                        String userEmail,
                        List<Map<String, String>> messages) {
        }

        private static final String SYSTEM_PROMPT = "당신은 SignBridge의 AI 어시스턴트입니다. " +
                        "수어(한국수어), 청각장애, 의사소통 방법 전문가입니다. " +
                        "친절하고 쉽게 한국어로 답변해 주세요. " +
                        "수어 동작은 구체적으로 묘사해 주세요. " +
                        "3~5문장으로 간결하게 답변해 주세요.";

        @PostMapping("/chat")
        public ResponseEntity<?> chat(@RequestBody ChatRequest req) {

                String key = geminiApiKey == null ? "" : geminiApiKey.trim();
                if (key.isBlank()) {
                        return ResponseEntity.status(503)
                                        .body(Map.of("error", "Gemini API 키가 설정되지 않았습니다."));
                }

                try {
                        // ── 대화 내역을 Gemini contents 형식으로 변환 ──────────
                        List<Map<String, Object>> contents = new ArrayList<>();

                        // 시스템 프롬프트를 첫 user 턴으로 삽입
                        contents.add(Map.of(
                                        "role", "user",
                                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))));
                        contents.add(Map.of(
                                        "role", "model",
                                        "parts", List.of(Map.of("text",
                                                        "네, 알겠습니다! SignBridge AI 어시스턴트로서 수어와 청각장애 소통을 도와드릴게요."))));

                        if (req.messages() != null) {
                                for (Map<String, String> msg : req.messages()) {
                                        String role = msg.get("role");
                                        String content = msg.get("content");
                                        if (content == null || content.isBlank())
                                                continue;

                                        // Gemini는 user / model 만 허용
                                        String geminiRole = "assistant".equals(role) ? "model" : "user";
                                        contents.add(Map.of(
                                                        "role", geminiRole,
                                                        "parts", List.of(Map.of("text", content))));
                                }
                        }

                        // 마지막이 user 턴이어야 함
                        if (contents.isEmpty() ||
                                        !"user".equals(contents.get(contents.size() - 1).get("role"))) {
                                contents.add(Map.of(
                                                "role", "user",
                                                "parts", List.of(Map.of("text", "안녕하세요"))));
                        }

                        // ── Gemini API 호출 ────────────────────────────────────
                        String url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                                        "gemini-2.5-flash:generateContent?key=" + key;

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        Map<String, Object> body = new HashMap<>();
                        body.put("contents", contents);
                        body.put("generationConfig", Map.of(
                                        "maxOutputTokens", 2048, // 600 → 2048
                                        "temperature", 0.7));

                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                        // ── 응답 파싱 ──────────────────────────────────────────
                        String replyText = "";
                        Map<String, Object> respBody = response.getBody();
                        if (respBody != null) {
                                List<?> candidates = (List<?>) respBody.get("candidates");
                                if (candidates != null && !candidates.isEmpty()) {
                                        Map<?, ?> first = (Map<?, ?>) candidates.get(0);
                                        Map<?, ?> contentMap = (Map<?, ?>) first.get("content");
                                        if (contentMap != null) {
                                                List<?> parts = (List<?>) contentMap.get("parts");
                                                if (parts != null && !parts.isEmpty()) {
                                                        Map<?, ?> part = (Map<?, ?>) parts.get(0);
                                                        replyText = String.valueOf(part.get("text"));
                                                }
                                        }
                                }
                        }

                        // 기존 앱 코드(OpenAI 형식)와 호환되게 감싸서 반환
                        Map<String, Object> result = Map.of(
                                        "choices", List.of(
                                                        Map.of("message", Map.of(
                                                                        "role", "assistant",
                                                                        "content", replyText))));

                        System.out.println("[AI] Gemini 응답 완료");
                        return ResponseEntity.ok(result);

                } catch (org.springframework.web.client.HttpClientErrorException e) {
                        System.err.println("[AI] Gemini HTTP 오류: " + e.getStatusCode());
                        System.err.println("[AI] 응답: " + e.getResponseBodyAsString());
                        return ResponseEntity.status(e.getStatusCode())
                                        .body(Map.of("error", "Gemini 오류 " + e.getStatusCode()));
                } catch (Exception e) {
                        System.err.println("[AI] 예외: " + e.getMessage());
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
                }
        }
}