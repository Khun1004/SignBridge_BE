package com.signbridge.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.PoliceCaseDto;
import com.signbridge.entity.PoliceCase;
import com.signbridge.repository.PoliceCaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GET /api/police/cases?email= — 케이스 목록 조회
 * POST /api/police/cases — 케이스 저장
 */
@Slf4j
@RestController
@RequestMapping("/api/police")
@RequiredArgsConstructor
public class PoliceController {

    private final PoliceCaseRepository policeCaseRepository;

    // ── GET: 목록 조회 ────────────────────────────────────────
    @GetMapping("/cases")
    public ResponseEntity<List<PoliceCaseDto.CaseItem>> getCases(
            @RequestParam(name = "email", required = false) String email) {

        if (email == null || email.isBlank())
            return ResponseEntity.ok(List.of());

        List<PoliceCase> cases = policeCaseRepository.findByUserEmailOrderByCaseDateDesc(email);

        return ResponseEntity.ok(
                cases.stream().map(this::toCaseItem).collect(Collectors.toList()));
    }

    // ── POST: 저장 ────────────────────────────────────────────
    @PostMapping("/cases")
    public ResponseEntity<?> saveCase(
            @RequestBody PoliceCaseDto.SaveRequest req) {
        try {
            String caseId = "POL-" + LocalDate.now().getYear() + "-"
                    + String.format("%04d", (int) (System.currentTimeMillis() % 10000));

            // extraVideoIds → JSON 문자열
            String extraIdsJson = null;
            if (req.getExtraVideoIds() != null && !req.getExtraVideoIds().isEmpty()) {
                extraIdsJson = req.getExtraVideoIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", "[", "]"));
            }

            PoliceCase entity = PoliceCase.builder()
                    .caseId(caseId)
                    .userEmail(req.getSafeEmail())
                    .officerName(req.getOfficerName())
                    .officerBadge(req.getOfficerBadge())
                    .officerRank(req.getOfficerRank())
                    .officerDepartment(req.getOfficerDepartment())
                    .officerStation(req.getOfficerStation())
                    .subjectName(req.getSubjectName())
                    .subjectRole(req.getSubjectRole())
                    .caseType(req.getCaseType())
                    .caseNumber(req.getCaseNumber())
                    .videoId(req.getVideoId())
                    .extraVideoIds(extraIdsJson)
                    .signs(req.getSigns() != null ? req.getSigns() : List.of())
                    .voice(req.getVoice() != null ? req.getVoice() : List.of())
                    .status("접수 완료")
                    .statusType("ok")
                    .flagged(false)
                    .caseDate(LocalDate.now())
                    .caseTime(LocalTime.now())
                    .build();

            policeCaseRepository.save(entity);
            log.info("[Police] 저장 완료 caseId={} email={}", caseId, req.getSafeEmail());

            return ResponseEntity.ok(
                    PoliceCaseDto.SaveResponse.builder()
                            .caseId(caseId)
                            .message("저장 완료")
                            .build());
        } catch (Exception e) {
            log.error("[Police] 저장 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("저장 실패: " + e.getMessage());
        }
    }

    // ── Entity → DTO ─────────────────────────────────────────
    private PoliceCaseDto.CaseItem toCaseItem(PoliceCase c) {
        List<Long> videoIds = new ArrayList<>();
        if (c.getVideoId() != null)
            videoIds.add(c.getVideoId());
        if (c.getExtraVideoIds() != null && !c.getExtraVideoIds().isBlank()) {
            try {
                String raw = c.getExtraVideoIds()
                        .replace("[", "").replace("]", "").trim();
                if (!raw.isEmpty()) {
                    for (String s : raw.split(","))
                        videoIds.add(Long.parseLong(s.trim()));
                }
            } catch (Exception e) {
                log.warn("[Police] extraVideoIds 파싱 실패: {}", e.getMessage());
            }
        }

        return PoliceCaseDto.CaseItem.builder()
                .id(c.getCaseId())
                .subject(PoliceCaseDto.SubjectInfo.builder()
                        .name(safe(c.getSubjectName()))
                        .birth(safe(c.getSubjectBirth()))
                        .disability(safe(c.getSubjectDisability()))
                        .nationality(safe(c.getSubjectNationality()))
                        .phone(safe(c.getSubjectPhone()))
                        .role(safe(c.getSubjectRole()))
                        .avatar("🧏")
                        .build())
                .officer(PoliceCaseDto.OfficerInfo.builder()
                        .name(safe(c.getOfficerName()))
                        .badge(safe(c.getOfficerBadge()))
                        .rank(safe(c.getOfficerRank()))
                        .department(safe(c.getOfficerDepartment()))
                        .station(safe(c.getOfficerStation()))
                        .avatar("👮")
                        .build())
                .caseType(safe(c.getCaseType()))
                .caseNumber(safe(c.getCaseNumber()))
                .date(c.getCaseDate() != null
                        ? c.getCaseDate().toString().replace("-", ".")
                        : "")
                .time(c.getCaseTime() != null
                        ? c.getCaseTime().toString().substring(0, 5)
                        : "")
                .location(safe(c.getLocation()))
                .duration(safe(c.getDuration()))
                .status(safe(c.getStatus()))
                .statusType(safe(c.getStatusType()))
                .flagged(c.isFlagged())
                .signs(c.getSigns() != null ? c.getSigns() : List.of())
                .voice(c.getVoice() != null ? c.getVoice() : List.of())
                .videoId(c.getVideoId())
                .videoIds(videoIds)
                .build();
    }

    private String safe(String v) {
        return v != null ? v : "";
    }
}