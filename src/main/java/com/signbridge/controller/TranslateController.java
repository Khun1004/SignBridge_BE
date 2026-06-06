package com.signbridge.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api")
public class TranslateController {

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/sign-guide")
    public ResponseEntity<?> getSignGuide(@RequestBody Map<String, String> req) {
        String text = req.get("text");
        if (text == null || text.isBlank())
            return ResponseEntity.badRequest().build();
        try {
            String systemPrompt = "당신은 한국 수어 전문가입니다. 입력 문장을 수어 동작(steps)으로 분해하세요.\n" +
                    "반드시 아래 animType 중 하나만 사용하세요 (다른 값 사용 금지):\n" +
                    "  hello, thumbUp, thumbDown, peace, ok, point,\n" +
                    "  love, stop, call, fist, thanks,\n" +
                    "  open, number, flat, cup, pinch, cross\n\n" +
                    "매핑 규칙:\n" +
                    "- 안녕하세요 / 반가워요 / 안녕히 계세요 → hello\n" +
                    "- 감사합니다 / 고맙습니다                → thanks\n" +
                    "- 좋아요 / 최고 / 잘했어요               → thumbUp\n" +
                    "- 싫어요 / 아니요 / 나빠요               → thumbDown\n" +
                    "- 알겠습니다 / 괜찮아요 / OK             → ok\n" +
                    "- 사랑합니다 / 사랑해요                  → love\n" +
                    "- 잠깐만요 / 기다려요 / 멈춰요           → stop\n" +
                    "- 평화 / V사인                           → peace\n" +
                    "- 전화해요 / 전화 주세요                 → call\n" +
                    "- 힘내요 / 화이팅                        → fist\n" +
                    "- 가리키기 / 저기요 / 여기                → point\n" +
                    "- 위의 규칙에 해당하지 않는 동사/형용사   → 가장 가까운 animType 선택\n\n" +
                    "응답은 반드시 JSON만, 마크다운 없이:\n" +
                    "{\"summary\":\"...\", \"steps\":[{\"word\":\"안녕\",\"animType\":\"hello\",\"description\":\"...\"}]}";

            String response = callClaude(systemPrompt, "문장: " + text);
            return ResponseEntity.ok(objectMapper.readValue(cleanJson(response), Map.class));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String callClaude(String system, String user) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", "claude-opus-4-6");
        body.put("max_tokens", 1000);
        body.put("system", system);

        ArrayNode msgs = objectMapper.createArrayNode();
        msgs.add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", user));
        body.set("messages", msgs);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        String res = restTemplate.postForObject(
                "https://api.anthropic.com/v1/messages", entity, String.class);

        return objectMapper.readTree(res)
                .path("content")
                .get(0)
                .path("text")
                .asText();
    }

    private String cleanJson(String raw) {
        if (raw == null)
            return "{}";
        raw = raw.trim();
        if (raw.startsWith("```json"))
            raw = raw.substring(7);
        else if (raw.startsWith("```"))
            raw = raw.substring(3);
        if (raw.endsWith("```"))
            raw = raw.substring(0, raw.length() - 3);
        return raw.trim();
    }
}