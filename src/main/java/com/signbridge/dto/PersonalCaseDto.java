package com.signbridge.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PersonalCaseDto {

    @Getter
    @NoArgsConstructor
    public static class SaveRequest {
        private String userEmail;
        private String name;
        private String memo;
        private Long videoId;
        private List<Long> extraVideoIds; // 추가 영상 IDs
        private String place;
        private List<MsgItem> messages;
    }

    @Getter
    @NoArgsConstructor
    public static class MsgItem {
        private String msgType;
        private String content;
        private String pose;
        private String sentAt;
    }

    @Getter
    @Builder
    public static class SaveResponse {
        private Long caseId;
        private String sessionId;
        private int savedCount;
        private String message;
    }

    /**
     * 목록 아이템
     * — videoIds: 대표 영상 + 추가 영상 전체 목록 (프론트에서 순회하여 표시)
     * — messages: 해당 세션의 대화 내용
     */
    @Getter
    @Builder
    public static class CaseItem {
        private Long id;
        private String name;
        private String memo;
        private Long videoId; // 대표 영상 (하위 호환)
        private List<Long> videoIds; // ← 전체 영상 ID 목록 (신규)
        private int messageCount;
        private String createdAt;
        private String sessionId;
        private List<MessageItem> messages;
    }

    @Getter
    @Builder
    public static class MessageItem {
        private String msgType;
        private String content;
        private String sentAt;
    }
}