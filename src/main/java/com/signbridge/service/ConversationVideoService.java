package com.signbridge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.signbridge.dto.ConversationVideoDto;
import com.signbridge.entity.ConversationVideo;
import com.signbridge.repository.ConversationVideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationVideoService {

        private final ConversationVideoRepository videoRepository;

        /** 영상 저장 디렉토리 — application.properties: signbridge.video.upload-dir=./videos */
        @Value("${signbridge.video.upload-dir:./videos}")
        private String uploadDir;

        /**
         * FFmpeg 실행 경로 — application.properties에서 설정 가능
         * Windows 예: signbridge.ffmpeg.path=C:/ffmpeg/bin/ffmpeg.exe
         * Linux/Mac : signbridge.ffmpeg.path=ffmpeg (PATH에 있으면 그냥 ffmpeg)
         */
        @Value("${signbridge.ffmpeg.path:ffmpeg}")
        private String ffmpegPath;

        /** webm → mp4 변환 여부 (false로 설정하면 webm 그대로 저장) */
        @Value("${signbridge.video.convert-to-mp4:true}")
        private boolean convertToMp4;

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // ════════════════════════════════════════════════════════════
        // 영상 업로드 — webm 저장 후 mp4 변환
        // ════════════════════════════════════════════════════════════
        public ConversationVideoDto.UploadResponse upload(MultipartFile file, String userEmail)
                        throws IOException {

                Path dir = Paths.get(uploadDir);
                Files.createDirectories(dir);

                // ── 1. webm 임시 저장 ────────────────────────────────────
                String originalName = file.getOriginalFilename() != null
                                ? file.getOriginalFilename()
                                : "recording.webm";
                // 타임스탬프 + UUID 앞 8자 — 동시 요청 시 충돌 방지
                String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
                long ts = System.currentTimeMillis();
                String webmName = "rec_" + ts + "_" + uid + ".webm";
                Path webmPath = dir.resolve(webmName);
                Files.copy(file.getInputStream(), webmPath, StandardCopyOption.REPLACE_EXISTING);

                // ── 2. mp4 변환 시도 ─────────────────────────────────────
                Path finalPath;
                String finalMime;
                String finalName;

                if (convertToMp4 && isFfmpegAvailable()) {
                        String mp4Name = "rec_" + ts + "_" + uid + ".mp4";
                        Path mp4Path = dir.resolve(mp4Name);
                        boolean ok = convertToMp4(webmPath, mp4Path);

                        if (ok) {
                                Files.deleteIfExists(webmPath) // webm 삭제
                                ;
                                finalPath = mp4Path;
                                finalMime = "video/mp4";
                                finalName = mp4Name;
                                log.info("[Video] webm → mp4 변환 완료: {}", mp4Name);
                        } else {
                                // 변환 실패 시 webm 그대로 유지
                                finalPath = webmPath;
                                finalMime = "video/webm";
                                finalName = webmName;
                                log.warn("[Video] mp4 변환 실패 — webm으로 저장: {}", webmName);
                        }
                } else {
                        // FFmpeg 없거나 변환 비활성화 → webm 그대로
                        finalPath = webmPath;
                        finalMime = file.getContentType() != null ? file.getContentType() : "video/webm";
                        finalName = webmName;
                        if (convertToMp4) {
                                log.warn("[Video] FFmpeg 없음 — webm으로 저장. " +
                                                "FFmpeg 설치 후 application.properties에 signbridge.ffmpeg.path 설정하세요.");
                        }
                }

                // ── 3. DB 저장 ───────────────────────────────────────────
                // userEmail 빈 문자열 → null (FK 제약 방지)
                String safeEmail = (userEmail != null && !userEmail.isBlank()) ? userEmail.trim() : null;

                ConversationVideo entity = ConversationVideo.builder()
                                .filePath(finalPath.toString())
                                .originalFilename(originalName)
                                .mimeType(finalMime)
                                .fileSize(Files.size(finalPath))
                                .userEmail(safeEmail)
                                .build();
                ConversationVideo saved = videoRepository.save(entity);

                String streamUrl = "/api/conversations/video/" + saved.getId();
                return ConversationVideoDto.UploadResponse.builder()
                                .videoId(saved.getId())
                                .url(streamUrl)
                                .filename(finalName)
                                .fileSize(Files.size(finalPath))
                                .message(finalMime.contains("mp4") ? "mp4로 변환 저장 완료" : "영상이 저장되었습니다.")
                                .build();
        }

        // ════════════════════════════════════════════════════════════
        // FFmpeg webm → mp4 변환
        // ════════════════════════════════════════════════════════════
        private boolean convertToMp4(Path input, Path output) {
                try {
                        // -y : 덮어쓰기 허용
                        // -i input : 입력 파일
                        // -c:v libx264 : H.264 비디오 코덱
                        // -preset fast : 인코딩 속도 (ultrafast/fast/medium/slow)
                        // -crf 23 : 품질 (0=최고, 51=최저, 23=기본)
                        // -c:a aac : AAC 오디오
                        // -movflags +faststart : 웹 스트리밍 최적화
                        ProcessBuilder pb = new ProcessBuilder(
                                        ffmpegPath, "-y",
                                        "-i", input.toAbsolutePath().toString(),
                                        "-c:v", "libx264",
                                        "-preset", "fast",
                                        "-crf", "23",
                                        "-c:a", "aac",
                                        "-movflags", "+faststart",
                                        output.toAbsolutePath().toString());
                        pb.redirectErrorStream(true) // stderr → stdout 합침
                        ;
                        Process proc = pb.start();
                        // 최대 120초 대기 (대용량 영상 고려)
                        boolean finished = proc.waitFor(120, TimeUnit.SECONDS);
                        if (!finished) {
                                proc.destroyForcibly();
                                log.warn("[FFmpeg] 타임아웃 — 120초 초과");
                                return false;
                        }
                        int exitCode = proc.exitValue();
                        if (exitCode != 0) {
                                log.warn("[FFmpeg] 오류 exit={}", exitCode);
                                return false;
                        }
                        return Files.exists(output) && Files.size(output) > 0;
                } catch (Exception e) {
                        log.error("[FFmpeg] 실행 오류: {}", e.getMessage());
                        return false;
                }
        }

        /** FFmpeg 사용 가능 여부 확인 */
        private boolean isFfmpegAvailable() {
                try {
                        Process p = new ProcessBuilder(ffmpegPath, "-version")
                                        .redirectErrorStream(true).start();
                        return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
                } catch (Exception e) {
                        return false;
                }
        }

        // ════════════════════════════════════════════════════════════
        // 영상 스트리밍
        // ════════════════════════════════════════════════════════════
        public Resource getVideoResource(Long videoId) {
                ConversationVideo video = videoRepository.findById(videoId)
                                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다: " + videoId));
                Path path = Paths.get(video.getFilePath());
                if (!Files.exists(path)) {
                        throw new RuntimeException("영상 파일이 존재하지 않습니다: " + path);
                }
                return new FileSystemResource(path);
        }

        public String getMimeType(Long videoId) {
                return videoRepository.findById(videoId)
                                .map(v -> v.getMimeType() != null ? v.getMimeType() : "video/mp4")
                                .orElse("video/mp4");
        }

        // ════════════════════════════════════════════════════════════
        // 목록 조회
        // ════════════════════════════════════════════════════════════
        public List<ConversationVideoDto.VideoItem> listByUser(String email) {
                return videoRepository.findByUserEmailOrderByCreatedAtDesc(email)
                                .stream()
                                .map(this::toVideoItem)
                                .collect(Collectors.toList());
        }

        public List<ConversationVideoDto.VideoItem> listByCase(String caseId) {
                return videoRepository.findByImmigrationCaseId(caseId)
                                .stream()
                                .map(this::toVideoItem)
                                .collect(Collectors.toList());
        }

        private ConversationVideoDto.VideoItem toVideoItem(ConversationVideo v) {
                return ConversationVideoDto.VideoItem.builder()
                                .videoId(v.getId())
                                .url("/api/conversations/video/" + v.getId())
                                .filename(v.getOriginalFilename())
                                .fileSize(v.getFileSize() != null ? v.getFileSize() : 0L)
                                .createdAt(v.getCreatedAt() != null ? v.getCreatedAt().format(FMT) : "")
                                .immigrationCaseId(v.getImmigrationCaseId())
                                .build();
        }

        // ════════════════════════════════════════════════════════════
        // 케이스 연결
        // ════════════════════════════════════════════════════════════
        public void linkToCase(Long videoId, String caseId) {
                ConversationVideo video = videoRepository.findById(videoId)
                                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다: " + videoId));
                video.setImmigrationCaseId(caseId);
                videoRepository.save(video);
        }
}