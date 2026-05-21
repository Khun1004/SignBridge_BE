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

import com.signbridge.dto.ImmigrationCaseDto;
import com.signbridge.entity.ImmigrationCase;
import com.signbridge.repository.ImmigrationCaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/immigration")
@RequiredArgsConstructor
public class ImmigrationController {

    private final ImmigrationCaseRepository immigrationCaseRepository;

    // ── GET: 케이스 목록 조회 ─────────────────────────────────
    @GetMapping("/cases")
    public ResponseEntity<List<ImmigrationCaseDto.CaseItem>> getCases(
            @RequestParam(name = "email", required = false) String email) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<ImmigrationCase> cases = immigrationCaseRepository.findByUserEmailOrderByCaseDateDesc(email);

        List<ImmigrationCaseDto.CaseItem> result = cases.stream()
                .map(this::toCaseItem)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── POST: 케이스 저장 ─────────────────────────────────────
    @PostMapping("/cases")
    public ResponseEntity<?> saveCase(
            @RequestBody ImmigrationCaseDto.SaveRequest req) {
        try {
            String caseId = "IMM-" + LocalDate.now().getYear() + "-"
                    + String.format("%04d", (int) (System.currentTimeMillis() % 10000));

            // extraVideoIds → JSON 문자열
            String extraIdsJson = null;
            if (req.getExtraVideoIds() != null && !req.getExtraVideoIds().isEmpty()) {
                extraIdsJson = req.getExtraVideoIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", "[", "]"));
            }

            ImmigrationCase entity = ImmigrationCase.builder()
                    .caseId(caseId)
                    .userEmail(req.getSafeEmail())
                    .officerName(req.getOfficerName())
                    .officerPosition(req.getOfficerPosition())
                    .officerDepartment(req.getOfficerDept())
                    .applicantName(req.getApplicantName())
                    .caseNumber(req.getCaseNumber())
                    .purpose(req.getPurpose())
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

            immigrationCaseRepository.save(entity);
            log.info("[Immigration] 저장 완료 caseId={} email={} videoId={}",
                    caseId, req.getSafeEmail(), req.getVideoId());

            return ResponseEntity.ok(
                    ImmigrationCaseDto.SaveResponse.builder()
                            .caseId(caseId)
                            .message("저장 완료")
                            .build());

        } catch (Exception e) {
            log.error("[Immigration] 저장 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("저장 실패: " + e.getMessage());
        }
    }

    // ── Entity → DTO 변환 ─────────────────────────────────────
    private ImmigrationCaseDto.CaseItem toCaseItem(ImmigrationCase c) {

        // 영상 ID 목록 조립
        List<Long> videoIds = new ArrayList<>();
        if (c.getVideoId() != null)
            videoIds.add(c.getVideoId());
        if (c.getExtraVideoIds() != null && !c.getExtraVideoIds().isBlank()) {
            try {
                String raw = c.getExtraVideoIds()
                        .replace("[", "").replace("]", "").trim();
                if (!raw.isEmpty()) {
                    for (String s : raw.split(",")) {
                        videoIds.add(Long.parseLong(s.trim()));
                    }
                }
            } catch (Exception e) {
                log.warn("[Immigration] extraVideoIds 파싱 실패: {}", e.getMessage());
            }
        }

        return ImmigrationCaseDto.CaseItem.builder()
                .id(c.getCaseId())
                .applicant(ImmigrationCaseDto.ApplicantInfo.builder()
                        .name(safe(c.getApplicantName()))
                        .birth(safe(c.getApplicantBirth()))
                        .disability(safe(c.getApplicantDisability()))
                        .nationality(safe(c.getApplicantNationality()))
                        .phone(safe(c.getApplicantPhone()))
                        .avatar("🧏")
                        .build())
                .officer(ImmigrationCaseDto.OfficerInfo.builder()
                        .name(safe(c.getOfficerName()))
                        .badge(safe(c.getOfficerBadge()))
                        .department(safe(c.getOfficerDepartment()))
                        .position(safe(c.getOfficerPosition()))
                        .avatar("👔")
                        .build())
                .purpose(safe(c.getPurpose()))
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