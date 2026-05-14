package com.signbridge.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class ConversationDto {

    /** 대화 기록 저장 요청 (POST /api/conversations) */
    @Getter
    @NoArgsConstructor
    public static class SaveRequest {
        private String userEmail;
        private String place;
        private Long videoId; // nullable
        private List<MsgItem> messages;
    }

    @Getter
    @NoArgsConstructor
    public static class MsgItem {
        private String msgType; // 'sign' | 'voice'
        private String content;
        private String pose; // nullable
        private String sentAt;
    }

    /** 저장 완료 응답 */
    @Getter
    @lombok.Builder
    public static class SaveResponse {
        private String sessionId;
        private int savedCount;
        private String message;
    }
}