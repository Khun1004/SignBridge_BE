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
     * body: { credential: "구글 ID 토큰" }
     * Google 토큰을 Google API로 직접 검증 (외부 라이브러리 불필요)
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");
        if (credential == null || credential.isBlank()) {
            return ResponseEntity.badRequest().body("credential이 없습니다.");
        }

        try {
            // ── Google tokeninfo API로 토큰 검증 ─────────────
            // 외부 라이브러리 없이 Google REST API 직접 호출
            String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
            String response = restTemplate.getForObject(verifyUrl, String.class);
            JsonNode payload = objectMapper.readTree(response);

            // aud(audience) 검증 — 내 클라이언트 ID와 일치해야 함
            String aud = payload.path("aud").asText("");
            if (!googleClientId.equals(aud)) {
                log.warn("[Google OAuth] aud 불일치: aud={}", aud);
                return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
            }

            String email = payload.path("email").asText();
            String name = payload.path("name").asText();
            String picture = payload.path("picture").asText();

            if (email.isBlank()) {
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
                // 신규 유저 자동 가입 (기본 개인 계정)
                user = User.builder()
                        .email(email)
                        .name(!name.isBlank() ? name : email.split("@")[0])
                        .password("") // Google 로그인은 비밀번호 없음
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