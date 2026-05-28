package com.signbridge.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client.id}")
    private String googleClientId;

    /**
     * POST /api/auth/google
     * body: { "credential": "ID 토큰" } ← 웹 방식
     * 또는
     * body: { "accessToken": "액세스 토큰" } ← 앱(expo-auth-session) 방식
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {

        String credential = body.get("credential"); // 웹 ID 토큰
        String accessToken = body.get("accessToken"); // 앱 Access 토큰

        // ── 둘 다 없으면 오류 ──────────────────────────────
        if ((credential == null || credential.isBlank()) &&
                (accessToken == null || accessToken.isBlank())) {
            return ResponseEntity.badRequest().body("credential 또는 accessToken이 필요합니다.");
        }

        try {
            String email;
            String name;
            String picture = "";

            if (accessToken != null && !accessToken.isBlank()) {
                // ── 앱 방식: Access Token → 사용자 정보 조회 ──
                log.info("[Google OAuth] Access Token 방식 (앱)");
                String userInfoUrl = "https://www.googleapis.com/userinfo/v2/me";
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
                org.springframework.http.ResponseEntity<String> resp = restTemplate.exchange(userInfoUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity, String.class);
                JsonNode payload = objectMapper.readTree(resp.getBody());
                email = payload.path("email").asText();
                name = payload.path("name").asText();
                picture = payload.path("picture").asText();

            } else {
                // ── 웹 방식: ID Token → tokeninfo 검증 ─────────
                log.info("[Google OAuth] ID Token 방식 (웹)");
                String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
                String response = restTemplate.getForObject(verifyUrl, String.class);
                JsonNode payload = objectMapper.readTree(response);

                // aud 검증
                String aud = payload.path("aud").asText("");
                if (!googleClientId.equals(aud)) {
                    log.warn("[Google OAuth] aud 불일치: aud={}", aud);
                    return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
                }

                email = payload.path("email").asText();
                name = payload.path("name").asText();
                picture = payload.path("picture").asText();
            }

            if (email == null || email.isBlank()) {
                return ResponseEntity.status(401).body("이메일 정보를 가져올 수 없습니다.");
            }

            log.info("[Google OAuth] 로그인 시도: email={}", email);

            // ── DB에서 유저 조회 또는 자동 생성 ──────────────
            Optional<User> existing = userRepository.findByEmail(email);
            User user;

            if (existing.isPresent()) {
                user = existing.get();
                log.info("[Google OAuth] 기존 유저 로그인: {}", email);
            } else {
                user = User.builder()
                        .email(email)
                        .name(!name.isBlank() ? name : email.split("@")[0])
                        .password("")
                        .orgType("personal")
                        .build();
                userRepository.save(user);
                log.info("[Google OAuth] 신규 유저 자동 가입: {}", email);
            }

            // ── 응답 ──────────────────────────────────────────
            Map<String, Object> result = new HashMap<>();
            result.put("email", user.getEmail());
            result.put("name", user.getName() != null ? user.getName() : name);
            result.put("orgType", user.getOrgType() != null ? user.getOrgType() : "personal");
            result.put("picture", picture);
            result.put("isNew", existing.isEmpty());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[Google OAuth] 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Google 로그인 처리 중 오류: " + e.getMessage());
        }
    }
}