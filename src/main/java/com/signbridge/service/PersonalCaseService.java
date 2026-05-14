package com.signbridge.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signbridge.dto.PersonalCaseDto;
import com.signbridge.entity.Conversation;
import com.signbridge.entity.PersonalCase;
import com.signbridge.repository.ConversationRepository;
import com.signbridge.repository.PersonalCaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalCaseService {

    private final PersonalCaseRepository personalCaseRepository;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ══════════════════════════════════════════════════════════
    // 저장
    // ══════════════════════════════════════════════════════════
    @Transactional
    public PersonalCaseDto.SaveResponse save(PersonalCaseDto.SaveRequest req) {
        String email = normalize(req.getUserEmail());
        String sessionId = UUID.randomUUID().toString();
        int savedCount = 0;

        // ① conversations 테이블에 메시지 저장
        if (req.getMessages() != null && !req.getMessages().isEmpty()) {
            List<Conversation> convEntities = req.getMessages().stream()
                    .map(m -> Conversation.builder()
                            .sessionId(sessionId)
                            .userEmail(email)
                            .place("personal")
                            .videoId(req.getVideoId())
                            .msgType(m.getMsgType())
                            .content(m.getContent())
                            .pose(m.getPose())
                            .sentAt(m.getSentAt())
                            .build())
                    .collect(Collectors.toList());
            conversationRepository.saveAll(convEntities);
            savedCount = convEntities.size();
            log.info("[PersonalCase] conversations 저장 count={} sessionId={}", savedCount, sessionId);
        }

        // ② extraVideoIds → JSON 문자열 변환
        String extraIdsJson = null;
        if (req.getExtraVideoIds() != null && !req.getExtraVideoIds().isEmpty()) {
            try {
                extraIdsJson = objectMapper.writeValueAsString(req.getExtraVideoIds());
            } catch (Exception e) {
                log.warn("[PersonalCase] extraVideoIds 직렬화 실패: {}", e.getMessage());
            }
        }

        // ③ personal_cases 저장
        PersonalCase entity = PersonalCase.builder()
                .name(req.getName())
                .userEmail(email)
                .memo(req.getMemo())
                .videoId(req.getVideoId())
                .extraVideoIds(extraIdsJson)
                .sessionId(sessionId)
                .messageCount(savedCount)
                .build();

        PersonalCase saved = personalCaseRepository.save(entity);
        log.info("[PersonalCase] 케이스 저장 id={} email={} videoId={} extraIds={}",
                saved.getId(), email, req.getVideoId(), extraIdsJson);

        return PersonalCaseDto.SaveResponse.builder()
                .caseId(saved.getId())
                .sessionId(sessionId)
                .savedCount(savedCount)
                .message("개인 대화 기록이 등록되었습니다.")
                .build();
    }

    // ══════════════════════════════════════════════════════════
    // 목록 조회 — videoIds 전체 + messages 포함
    // ══════════════════════════════════════════════════════════
    public List<PersonalCaseDto.CaseItem> listByUser(String email) {
        return personalCaseRepository
                .findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(c -> {
                    // ── 전체 영상 ID 목록 조립 ──────────────────
                    List<Long> allVideoIds = new ArrayList<>();
                    if (c.getVideoId() != null) {
                        allVideoIds.add(c.getVideoId());
                    }
                    if (c.getExtraVideoIds() != null && !c.getExtraVideoIds().isBlank()) {
                        try {
                            List<Long> extras = objectMapper.readValue(
                                    c.getExtraVideoIds(),
                                    new TypeReference<List<Long>>() {
                                    });
                            allVideoIds.addAll(extras);
                        } catch (Exception e) {
                            log.warn("[PersonalCase] extraVideoIds 파싱 실패 id={}: {}",
                                    c.getId(), e.getMessage());
                        }
                    }

                    // ── 대화 내용 조회 ──────────────────────────
                    List<PersonalCaseDto.MessageItem> msgs = new ArrayList<>();
                    if (c.getSessionId() != null) {
                        msgs = conversationRepository
                                .findBySessionIdOrderByCreatedAtAsc(c.getSessionId())
                                .stream()
                                .map(m -> PersonalCaseDto.MessageItem.builder()
                                        .msgType(m.getMsgType())
                                        .content(m.getContent())
                                        .sentAt(m.getSentAt())
                                        .build())
                                .collect(Collectors.toList());
                        log.debug("[PersonalCase] id={} sessionId={} msgs={}",
                                c.getId(), c.getSessionId(), msgs.size());
                    }

                    return PersonalCaseDto.CaseItem.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .memo(c.getMemo())
                            .videoId(c.getVideoId()) // 하위 호환
                            .videoIds(allVideoIds) // ← 전체 목록
                            .messageCount(c.getMessageCount() != null ? c.getMessageCount() : 0)
                            .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : "")
                            .sessionId(c.getSessionId())
                            .messages(msgs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── 케이스 단건 삭제 ─────────────────────────────────────
    @Transactional
    public void deleteCase(Long id) {
        PersonalCase c = personalCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("케이스를 찾을 수 없습니다: " + id));
        if (c.getSessionId() != null) {
            conversationRepository.deleteBySessionId(c.getSessionId());
        }
        personalCaseRepository.delete(c);
        log.info("[PersonalCase] 삭제 id={}", id);
    }

    // ── 세션 기준 삭제 ────────────────────────────────────────
    @Transactional
    public void deleteBySession(String sessionId) {
        conversationRepository.deleteBySessionId(sessionId);
        personalCaseRepository.deleteBySessionId(sessionId);
        log.info("[PersonalCase] 세션 삭제 sessionId={}", sessionId);
    }

    private String normalize(String email) {
        return (email != null && !email.isBlank()) ? email.trim() : null;
    }
}