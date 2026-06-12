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

    // ── 전체 목록 조회 ─────────────────────────────────────
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

    // ── 내 게시물 목록 조회 ────────────────────────────────
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyPosts(@RequestParam("email") String email) {
        List<CommunityMember> list = repo.findByUserEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ── 단건 조회 ─────────────────────────────────────────
    @GetMapping("/members/{id}")
    public ResponseEntity<?> getMember(@PathVariable Long id) {
        return repo.findById(id)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 채팅 ID 중복 확인 ─────────────────────────────────
    @GetMapping("/check-chat-id")
    public ResponseEntity<Map<String, Boolean>> checkChatId(
            @RequestParam String chatId,
            @RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            // Skip check if it's the user's own existing chatId
            boolean isOwn = repo.findByUserEmail(email)
                    .map(m -> chatId.equals(m.getChatId()))
                    .orElse(false);
            if (isOwn) return ResponseEntity.ok(Map.of("available", true));
        }
        boolean available = !repo.existsByChatId(chatId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // ── 새 게시물 등록 (always creates new) ───────────────
    @PostMapping("/members")
    public ResponseEntity<?> register(@RequestBody CommunityMemberDto.Request req) {
        // Validate chatId ownership before creating new post
        if (req.getChatId() != null && !req.getChatId().isBlank()) {
            if (repo.existsByChatId(req.getChatId())) {
                boolean isOwn = repo.findByChatId(req.getChatId())
                        .map(m -> m.getUserEmail().equals(req.getUserEmail()))
                        .orElse(false);
                if (!isOwn) {
                    return ResponseEntity.badRequest().body("이미 사용 중인 채팅 ID입니다.");
                }
                // It's their own chatId — clear it from request so applyRequest skips it
                // We'll set it manually after
            }
        }

        CommunityMember member = new CommunityMember();
        applyRequest(member, req);
        // Set chatId directly after applyRequest (applyRequest no longer touches chatId)
        member.setChatId(req.getChatId());
        repo.save(member);
        log.info("[Community] 새 게시물 등록: {} ({})", req.getName(), req.getUserEmail());
        return ResponseEntity.ok(toResponse(member));
    }

    // ── 게시물 수정 ────────────────────────────────────────
    @PutMapping("/members/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
            @RequestBody CommunityMemberDto.Request req) {
        return repo.findById(id).map(member -> {
            if (!member.getUserEmail().equals(req.getUserEmail())) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }
            applyRequest(member, req);
            // chatId is locked — never change on update
            repo.save(member);
            log.info("[Community] 수정: id={}", id);
            return ResponseEntity.ok(toResponse(member));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── 게시물 삭제 ────────────────────────────────────────
    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
            @RequestParam("email") String email) {
        return repo.findById(id).map(member -> {
            if (!member.getUserEmail().equals(email)) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }
            repo.delete(member);
            log.info("[Community] 삭제: id={}", id);
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
            file.transferTo(uploadPath.resolve(savedName).toFile());
            return ResponseEntity.ok(Map.of("fileName", savedName, "originalName", originalName));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // ── Entity → Dto ─────────────────────────────────────
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
                .avatar(m.getName() != null ? String.valueOf(m.getName().charAt(0)) : "?")
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    // ── Request → Entity ─────────────────────────────────
    private void applyRequest(CommunityMember m, CommunityMemberDto.Request req) {
        m.setName(req.getName());
        m.setUserEmail(req.getUserEmail());
        m.setRole(req.getRole());
        m.setRegion(req.getRegion());
        m.setIntro(req.getIntro());
        m.setExperience(req.getExperience());
        m.setSpeciality(req.getSpeciality());
        m.setContactType(req.getContactType());
        m.setContactValue(req.getContactValue());
        m.setPublicProfile(req.getPublicProfile() != null ? req.getPublicProfile() : true);
        // NOTE: chatId is NOT set here — handled separately in register() and update()
        if (req.getCertFileNames() != null) {
            m.setCertFileNames(String.join(",", req.getCertFileNames()));
        }
    }
}