package com.signbridge.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.signbridge.dto.CommunityMemberDto;
import com.signbridge.entity.CommunityMember;
import com.signbridge.repository.CommunityMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityMemberRepository repo;

    private static final String UPLOAD_DIR = "uploads/community/certs/";

    // ── 전체 목록 조회 (필터 포함) ────────────────────────
    @GetMapping("/members")
    public ResponseEntity<?> getMembers(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "keyword", required = false) String keyword) {

        List<CommunityMember> list;

        if (keyword != null && !keyword.isBlank()) {
            list = repo.searchByKeyword(keyword.trim());
        } else if (role != null && !role.isBlank() && region != null && !region.isBlank()) {
            list = repo.findByRoleAndRegionAndPublicProfileTrueOrderByCreatedAtDesc(role, region);
        } else if (role != null && !role.isBlank()) {
            list = repo.findByRoleAndPublicProfileTrueOrderByCreatedAtDesc(role);
        } else if (region != null && !region.isBlank()) {
            list = repo.findByRegionAndPublicProfileTrueOrderByCreatedAtDesc(region);
        } else {
            list = repo.findByPublicProfileTrueOrderByCreatedAtDesc();
        }

        return ResponseEntity.ok(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ── 내 프로필 목록 조회 (여러 개) ────────────────────
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyProfiles(@RequestParam("email") String email) {
        List<CommunityMember> list = repo.findByUserEmail(email);
        return ResponseEntity.ok(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ── 단건 조회 ─────────────────────────────────────────
    @GetMapping("/members/{id}")
    public ResponseEntity<?> getMember(@PathVariable("id") Long id) {
        return repo.findById(id)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 채팅 ID 중복 확인 ─────────────────────────────────
    @GetMapping("/check-chat-id")
    public ResponseEntity<?> checkChatId(@RequestParam("chatId") String chatId) {
        boolean exists = repo.existsByChatId(chatId.trim());
        return ResponseEntity.ok(Map.of("available", !exists));
    }

    // ── 등록 ─────────────────────────────────────────────
    @PostMapping("/members")
    public ResponseEntity<?> register(@RequestBody CommunityMemberDto.Request req) {
        // id가 있으면 수정
        if (req.getId() != null) {
            return repo.findById(req.getId()).map(member -> {
                applyRequest(member, req);
                repo.save(member);
                log.info("[Community] 수정: id={}", req.getId());
                return ResponseEntity.ok(toResponse(member));
            }).orElse(ResponseEntity.notFound().build());
        }

        // 신규 등록 — 같은 역할이 이미 있으면 거부
        if (req.getRole() != null && !req.getRole().isBlank()) {
            boolean exists = repo.existsByUserEmailAndRole(req.getUserEmail(), req.getRole());
            if (exists) {
                return ResponseEntity.badRequest()
                        .body("이미 '" + req.getRole() + "' 역할로 등록된 프로필이 있습니다.");
            }
        }

        CommunityMember member = new CommunityMember();
        applyRequest(member, req);
        repo.save(member);

        log.info("[Community] 등록: {} ({})", req.getName(), req.getUserEmail());
        return ResponseEntity.ok(toResponse(member));
    }

    // ── 수정 ──────────────────────────────────────────────
    @PutMapping("/members/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
            @RequestBody CommunityMemberDto.Request req) {
        return repo.findById(id).map(member -> {
            applyRequest(member, req);
            repo.save(member);
            log.info("[Community] 수정: id={}", id);
            return ResponseEntity.ok(toResponse(member));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── 삭제 (id 기반) ────────────────────────────────────
    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id,
            @RequestParam("email") String email) {
        return repo.findById(id).map(member -> {
            if (!member.getUserEmail().equals(email)) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }
            repo.delete(member);
            log.info("[Community] 삭제 (id): id={}", id);
            return ResponseEntity.ok(Map.of("message", "삭제 완료"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── 자격증 파일 업로드 ────────────────────────────────
    @PostMapping("/members/cert-upload")
    public ResponseEntity<?> uploadCert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR + email.replace("@", "_").replace(".", "_") + "/");
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String savedName = System.currentTimeMillis() + "_" + originalName;
            Path filePath = uploadPath.resolve(savedName);
            file.transferTo(filePath.toFile());

            log.info("[Community] 자격증 업로드: {} → {}", email, savedName);
            return ResponseEntity.ok(Map.of(
                    "fileName", savedName,
                    "originalName", originalName));
        } catch (Exception e) {
            log.error("[Community] 파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // ── 유틸: Entity → Dto ───────────────────────────────
    private CommunityMemberDto.Response toResponse(CommunityMember m) {
        List<String> certFiles = (m.getCertFileNames() != null && !m.getCertFileNames().isBlank())
                ? Arrays.asList(m.getCertFileNames().split(","))
                : new ArrayList<>();

        return CommunityMemberDto.Response.builder()
                .id(m.getId())
                .name(m.getName())
                .chatId(m.getChatId())
                .userEmail(m.getUserEmail())
                .role(m.getRole())
                .region(m.getRegion())
                .intro(m.getIntro())
                .experience(m.getExperience())
                .speciality(m.getSpeciality())
                .contactType(m.getContactType())
                .contactValue(m.getContactValue())
                .publicProfile(m.getPublicProfile())
                .certFileNames(certFiles)
                .avatar(m.getName() != null && !m.getName().isBlank()
                        ? String.valueOf(m.getName().charAt(0))
                        : "?")
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    // ── 유틸: Request → Entity 적용 ─────────────────────
    private void applyRequest(CommunityMember m, CommunityMemberDto.Request req) {
        m.setName(req.getName());
        m.setUserEmail(req.getUserEmail());
        // chatId: 신규일 때만 설정
        if (m.getChatId() == null && req.getChatId() != null && !req.getChatId().isBlank()) {
            m.setChatId(req.getChatId().trim());
        }
        m.setRole(req.getRole());
        m.setRegion(req.getRegion());
        m.setIntro(req.getIntro());
        m.setExperience(req.getExperience());
        m.setSpeciality(req.getSpeciality());
        m.setContactType(req.getContactType());
        m.setContactValue(req.getContactValue());
        m.setPublicProfile(req.getPublicProfile() != null ? req.getPublicProfile() : true);
        if (req.getCertFileNames() != null) {
            m.setCertFileNames(String.join(",", req.getCertFileNames()));
        }
    }
}