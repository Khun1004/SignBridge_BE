package com.signbridge.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.signbridge.dto.ConversationVideoDto;
import com.signbridge.service.ConversationVideoService;

import lombok.RequiredArgsConstructor;

/**
 * POST /api/conversations/video — 영상 업로드
 * GET /api/conversations/video/{id} — 영상 스트리밍 / 다운로드
 * GET /api/conversations/videos — 사용자 영상 목록
 * GET /api/conversations/videos/case/{caseId} — 케이스 연결 영상 목록
 * PATCH /api/conversations/video/{id}/link — 케이스와 연결
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationVideoController {

    private final ConversationVideoService videoService;

    // ── 영상 업로드 ────────────────────────────────────────────
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConversationVideoDto.UploadResponse> upload(
            @RequestPart("video") MultipartFile videoFile,
            @RequestPart(value = "email", required = false) String email) throws IOException {
        ConversationVideoDto.UploadResponse res = videoService.upload(videoFile, email);
        return ResponseEntity.ok(res);
    }

    // ── 영상 스트리밍 (브라우저 <video> 태그용) ────────────────
    @GetMapping("/video/{id}")
    public ResponseEntity<Resource> stream(@PathVariable Long id) {
        Resource resource = videoService.getVideoResource(id);
        String mime = videoService.getMimeType(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"recording_" + id + "\"")
                .body(resource);
    }

    // ── 영상 강제 다운로드 ─────────────────────────────────────
    @GetMapping("/video/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = videoService.getVideoResource(id);
        String mime = videoService.getMimeType(id);
        String ext = mime.contains("mp4") ? ".mp4" : ".webm";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"signbridge_recording_" + id + ext + "\"")
                .body(resource);
    }

    // ── 사용자별 영상 목록 ─────────────────────────────────────
    @GetMapping("/videos")
    public ResponseEntity<List<ConversationVideoDto.VideoItem>> listByUser(
            @RequestParam String email) {
        return ResponseEntity.ok(videoService.listByUser(email));
    }

    // ── 케이스에 연결된 영상 목록 ──────────────────────────────
    @GetMapping("/videos/case/{caseId}")
    public ResponseEntity<List<ConversationVideoDto.VideoItem>> listByCase(
            @PathVariable String caseId) {
        return ResponseEntity.ok(videoService.listByCase(caseId));
    }

    // ── 케이스와 영상 연결 ─────────────────────────────────────
    @PatchMapping("/video/{id}/link")
    public ResponseEntity<Void> linkToCase(
            @PathVariable Long id,
            @RequestParam String caseId) {
        videoService.linkToCase(id, caseId);
        return ResponseEntity.ok().build();
    }
}