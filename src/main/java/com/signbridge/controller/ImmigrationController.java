package com.signbridge.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.entity.ImmigrationCase;
import com.signbridge.repository.ImmigrationCaseRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/immigration")
@RequiredArgsConstructor
public class ImmigrationController {

    private final ImmigrationCaseRepository immigrationCaseRepository;

    // ── GET: 기관 이메일로 케이스 목록 조회 ──
    @GetMapping("/cases")
    public ResponseEntity<?> getCases(@RequestParam(required = false) String email) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<ImmigrationCase> cases = immigrationCaseRepository
                .findByUserEmailOrderByCaseDateDesc(email);

        List<Map<String, Object>> result = new ArrayList<>();

        for (ImmigrationCase c : cases) {

            Map<String, Object> applicant = new HashMap<>();
            applicant.put("name", c.getApplicantName());
            applicant.put("birth", c.getApplicantBirth());
            applicant.put("disability", c.getApplicantDisability());
            applicant.put("nationality", c.getApplicantNationality());
            applicant.put("phone", c.getApplicantPhone());
            applicant.put("avatar", "🧏");

            Map<String, Object> officer = new HashMap<>();
            officer.put("name", c.getOfficerName());
            officer.put("badge", c.getOfficerBadge());
            officer.put("department", c.getOfficerDepartment());
            officer.put("position", c.getOfficerPosition());
            officer.put("avatar", "👔");

            Map<String, Object> caseMap = new HashMap<>();
            caseMap.put("id", c.getCaseId());
            caseMap.put("applicant", applicant);
            caseMap.put("officer", officer);
            caseMap.put("purpose", c.getPurpose());
            caseMap.put("date", c.getCaseDate() != null ? c.getCaseDate().toString().replace("-", ".") : "");
            caseMap.put("time", c.getCaseTime() != null ? c.getCaseTime().toString().substring(0, 5) : "");
            caseMap.put("location", c.getLocation());
            caseMap.put("duration", c.getDuration());
            caseMap.put("status", c.getStatus());
            caseMap.put("statusType", c.getStatusType());
            caseMap.put("flagged", c.isFlagged());
            caseMap.put("signs", c.getSigns() != null ? c.getSigns() : List.of());
            caseMap.put("voice", c.getVoice() != null ? c.getVoice() : List.of());

            result.add(caseMap);
        }

        return ResponseEntity.ok(result);
    }

    // ── POST: 대화 종료 후 출입국 케이스 저장 ──
    // RegisterImmigration.jsx → immigrationApi.saveRecord(data) 로 호출됨
    @PostMapping("/cases")
    public ResponseEntity<?> saveCase(@RequestBody Map<String, Object> body) {
        try {
            String caseId = "IMM-" + LocalDate.now().getYear() + "-"
                    + String.format("%04d", (int) (System.currentTimeMillis() % 10000));

            @SuppressWarnings("unchecked")
            List<String> signs = body.get("signs") instanceof List
                    ? (List<String>) body.get("signs")
                    : List.of();
            @SuppressWarnings("unchecked")
            List<String> voice = body.get("voice") instanceof List
                    ? (List<String>) body.get("voice")
                    : List.of();

            ImmigrationCase newCase = ImmigrationCase.builder()
                    .caseId(caseId)
                    .userEmail(str(body, "userEmail"))
                    .applicantName(str(body, "applicantName"))
                    .officerName(str(body, "officerName"))
                    .purpose(str(body, "purpose"))
                    .status("접수 완료")
                    .statusType("ok")
                    .flagged(false)
                    .caseDate(LocalDate.now())
                    .caseTime(LocalTime.now())
                    .signs(signs)
                    .voice(voice)
                    .build();

            immigrationCaseRepository.save(newCase);
            return ResponseEntity.ok(Map.of("caseId", caseId, "message", "저장 완료"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("저장 실패: " + e.getMessage());
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }
}