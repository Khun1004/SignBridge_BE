package com.signbridge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.ConversationDto;
import com.signbridge.entity.Conversation;
import com.signbridge.service.ConversationService;

import lombok.RequiredArgsConstructor;

/**
 * POST /api/conversations — 대화 기록 저장
 * GET /api/conversations?email=... — 사용자 대화 목록
 * GET /api/conversations/{sessionId} — 세션 상세
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // ── 대화 기록 저장 ────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> save(@RequestBody ConversationDto.SaveRequest req) {
        try {
            ConversationDto.SaveResponse res = conversationService.save(req);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            // userEmail 누락 등 클라이언트 입력 오류 → 400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── 사용자별 목록 ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Conversation>> listByUser(
            @RequestParam("email") String email) {
        return ResponseEntity.ok(conversationService.listByUser(email));
    }

    // ── 세션 상세 ─────────────────────────────────────────────
    @GetMapping("/{sessionId}")
    public ResponseEntity<List<Conversation>> getSession(
            @PathVariable("sessionId") String sessionId) {
        return ResponseEntity.ok(conversationService.getSession(sessionId));
    }
}