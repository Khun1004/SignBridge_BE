package com.signbridge.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.signbridge.dto.ConversationDto;
import com.signbridge.entity.Conversation;
import com.signbridge.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    // ── 대화 기록 저장 ────────────────────────────────────────
    @Transactional
    public ConversationDto.SaveResponse save(ConversationDto.SaveRequest req) {

        // ① 메시지 없으면 조기 반환
        if (req.getMessages() == null || req.getMessages().isEmpty()) {
            return ConversationDto.SaveResponse.builder()
                    .savedCount(0)
                    .message("저장할 메시지가 없습니다.")
                    .build();
        }

        // ② 빈 문자열 → null 변환
        // FK 조건: user_email NULL은 허용, ""(빈 문자열)은 users 테이블에 없으므로 FK 위반
        String email = (req.getUserEmail() != null && !req.getUserEmail().isBlank())
                ? req.getUserEmail().trim()
                : null;

        // 한 대화 세션 = 하나의 UUID
        String sessionId = UUID.randomUUID().toString();

        List<Conversation> entities = req.getMessages().stream()
                .map(m -> Conversation.builder()
                        .sessionId(sessionId)
                        .userEmail(email)
                        .place(req.getPlace())
                        .videoId(req.getVideoId())
                        .msgType(m.getMsgType())
                        .content(m.getContent())
                        .pose(m.getPose())
                        .sentAt(m.getSentAt())
                        .build())
                .collect(Collectors.toList());

        conversationRepository.saveAll(entities);

        return ConversationDto.SaveResponse.builder()
                .sessionId(sessionId)
                .savedCount(entities.size())
                .message(email != null
                        ? "대화 기록이 저장되었습니다."
                        : "대화 기록이 저장되었습니다. (비로그인 — 이메일 미연결)")
                .build();
    }

    // ── 사용자 대화 기록 목록 ─────────────────────────────────
    public List<Conversation> listByUser(String email) {
        return conversationRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    // ── 세션 상세 조회 ────────────────────────────────────────
    public List<Conversation> getSession(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}