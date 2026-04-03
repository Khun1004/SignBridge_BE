package com.signbridge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api")
public class TranslateController {

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-3-5-sonnet-20240620"; // 최신 안정화 모델 권장
    private static final String ANTHROPIC_VER = "2023-06-01";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String[]> PLACE_CONTEXT = Map.of(
            "hospital", new String[] { "병원", "의사·간호사에게 증상을 설명하는 상황. 진료·처방·통증 관련 어휘를 우선 사용하세요." },
            "immigration", new String[] { "출입국관리사무소", "담당자에게 비자·체류·여권 업무를 요청하는 상황. 행정 용어를 명확하게 사용하세요." },
            "school", new String[] { "학교", "선생님·교직원과 대화하는 상황. 학습·수업·학교생활 관련 어휘를 우선 사용하세요." },
            "airport", new String[] { "공항", "항공사·출입국 직원에게 탑승·수화물·이동을 문의하는 상황. 항공 용어를 명확하게 사용하세요." },
            "police", new String[] { "경찰서", "경찰관에게 사건·신고·피해를 전달하는 상황. 정확하고 간결하게 사실을 전달하세요." });

    // [POST] /api/subtitle : 수어 단어 -> 한국어 문장
    @PostMapping("/subtitle")
    public ResponseEntity<?> buildSubtitle(@RequestBody SubtitleRequest req) {
        if (req.getWords() == null || req.getWords().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "words가 비어있습니다."));

        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            String fallback = String.join(" ", req.getWords()) + ".";
            return ResponseEntity.ok(Map.of("sentence", fallback, "source", "fallback"));
        }

        try {
            String sentence = callClaudeForSubtitle(req.getWords(), req.getPlace(), req.getPrevSentence());
            return ResponseEntity.ok(Map.of("sentence", sentence, "source", "claude"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("sentence", String.join(" ", req.getWords()) + ".", "source", "fallback"));
        }
    }

    // [POST] /api/sign-guide : 텍스트 -> 정밀 수어 시연 데이터 (수정됨)
    @PostMapping("/sign-guide")
    public ResponseEntity<?> getSignGuide(@RequestBody SignGuideRequest req) {
        if (req.getText() == null || req.getText().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "text가 비어있습니다."));

        if (anthropicApiKey == null || anthropicApiKey.isBlank())
            return ResponseEntity.ok(buildFallbackGuide(req.getText()));

        try {
            Map<String, Object> guide = callClaudeForSignGuide(req.getText());
            return ResponseEntity.ok(guide);
        } catch (Exception e) {
            return ResponseEntity.ok(buildFallbackGuide(req.getText()));
        }
    }

    private String callClaudeForSubtitle(List<String> words, String place, String prevSentence) throws Exception {
        String[] ctx = PLACE_CONTEXT.getOrDefault(place, PLACE_CONTEXT.get("immigration"));
        String systemPrompt = String.format(
                "수어 인식 시스템. 장소: %s (%s). 규칙: 1.자연스러운 존댓말 2.JSON 출력: {\"sentence\":\"...\"}",
                ctx[0], ctx[1]);
        String userContent = "인식 단어: [" + String.join(", ", words) + "], 이전 문장: "
                + (prevSentence != null ? prevSentence : "없음");

        String responseText = callClaude(systemPrompt, userContent, 500);
        JsonNode node = objectMapper.readTree(cleanJson(responseText));
        return node.path("sentence").asText(String.join(" ", words));
    }

    // ★ 핵심 수정 부분: 정밀 수어 데이터 추출 로직 ★
    private Map<String, Object> callClaudeForSignGuide(String text) throws Exception {
        String systemPrompt = "당신은 한국 수어(KSL) 전문가이자 3D 애니메이션 엔지니어입니다.\n" +
                "텍스트를 수어 동작 데이터로 변환하세요. 각 동작은 로봇/아바타가 읽을 수 있도록 수치화되어야 합니다.\n\n" +
                "규칙:\n" +
                "1. steps 배열의 각 객체에 'jointData'를 포함하세요.\n" +
                "2. jointData 구조:\n" +
                "   - fingerFlex: [엄지, 검지, 중지, 약지, 소지] 순서로 0.0(완전히 폄) ~ 1.0(완전히 굽힘)\n" +
                "   - palmOrientation: 손바닥 방향 (Forward, Backward, Up, Down, Inward, Outward)\n" +
                "   - position: 가슴 중앙(0,0,0) 기준 손의 좌표 (x, y, z)\n" +
                "3. movement: 이동 궤적을 '직선', '원형', '반복' 등으로 명시하세요.\n" +
                "4. expression: '입모양(Mouth)', '눈썹(Eyebrow)' 값을 포함하세요.\n\n" +
                "JSON 형식 예시:\n" +
                "{\n" +
                "  \"summary\": \"내용\",\n" +
                "  \"steps\": [\n" +
                "    {\n" +
                "      \"word\": \"단어\",\n" +
                "      \"jointData\": { \"fingerFlex\": [0, 0, 1, 1, 1], \"palmOrientation\": \"Up\", \"position\": [10, 0, 5] },\n"
                +
                "      \"animType\": \"...\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String userContent = "다음 문장의 수어 동작 데이터를 생성하세요: \"" + text + "\"";
        String responseText = callClaude(systemPrompt, userContent, 2000);
        return objectMapper.readValue(cleanJson(responseText), Map.class);
    }

    private String callClaude(String systemPrompt, String userContent, int maxTokens) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", ANTHROPIC_VER);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", CLAUDE_MODEL);
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(objectMapper.createObjectNode().put("role", "user").put("content", userContent));
        body.set("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        ResponseEntity<String> response = restTemplate.exchange(CLAUDE_URL, HttpMethod.POST, entity, String.class);
        return objectMapper.readTree(response.getBody()).path("content").get(0).path("text").asText();
    }

    private String cleanJson(String raw) {
        return raw.replaceAll("```json", "").replaceAll("```", "").trim();
    }

    private Map<String, Object> buildFallbackGuide(String text) {
        return Map.of("summary", text, "tip", "API 키 확인 필요", "steps", List.of());
    }

    // DTO 클래스들 (기존과 동일)
    static class SubtitleRequest {
        private List<String> words;
        private String place;
        private String prevSentence;

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> w) {
            this.words = w;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String p) {
            this.place = p;
        }

        public String getPrevSentence() {
            return prevSentence;
        }

        public void setPrevSentence(String s) {
            this.prevSentence = s;
        }
    }

    static class SignGuideRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String t) {
            this.text = t;
        }
    }
}