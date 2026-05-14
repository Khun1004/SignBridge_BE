package com.signbridge.dto;

import lombok.Builder;
import lombok.Getter;

public class ConversationVideoDto {

    /** 영상 업로드 응답 */
    @Getter
    @Builder
    public static class UploadResponse {
        private Long videoId;
        private String url; // /api/conversations/video/{id} 스트림 URL
        private String filename;
        private long fileSize;
        private String message;
    }

    /** 영상 목록 아이템 */
    @Getter
    @Builder
    public static class VideoItem {
        private Long videoId;
        private String url;
        private String filename;
        private long fileSize;
        private String createdAt;
        private String immigrationCaseId; // 연결된 케이스 (있으면)
    }
}