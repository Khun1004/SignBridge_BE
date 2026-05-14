package com.signbridge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.PersonalCaseDto;
import com.signbridge.service.PersonalCaseService;

import lombok.RequiredArgsConstructor;

/**
 * POST /api/personal/cases — 저장
 * GET /api/personal/cases?email= — 목록 (messages 포함)
 * DELETE /api/personal/cases/{id} — 단건 삭제
 * DELETE /api/personal/cases/session/{sessionId} — 세션 삭제
 */
@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalCaseController {

    private final PersonalCaseService personalCaseService;

    @PostMapping("/cases")
    public ResponseEntity<?> save(@RequestBody PersonalCaseDto.SaveRequest req) {
        try {
            return ResponseEntity.ok(personalCaseService.save(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/cases")
    public ResponseEntity<List<PersonalCaseDto.CaseItem>> list(@RequestParam String email) {
        return ResponseEntity.ok(personalCaseService.listByUser(email));
    }

    @DeleteMapping("/cases/{id}")
    public ResponseEntity<?> deleteCase(@PathVariable Long id) {
        try {
            personalCaseService.deleteCase(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cases/session/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        try {
            personalCaseService.deleteBySession(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}