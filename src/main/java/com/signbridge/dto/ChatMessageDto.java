package com.signbridge.dto;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessageDto {

    private Long   id;           // null on send, filled on broadcast
    private String roomId;
    private String senderEmail;
    private String senderName;
    private String text;
    private String fileName;
    private String fileUrl;
    private Boolean isImage;
    private Long    replyToId;
    private String  replyToName;
    private String  replyToText;
    private String  forwardedFrom;
    private Boolean isSystem;
    private Boolean isEdited;
    private String  sentAt;       // ISO string — easier for React

    // Message type: SEND | EDIT | DELETE | JOIN | REACTION
    private String type;

    // For reactions: { email: emoji }
    private String reactionEmoji;   // the emoji being set/removed
    private String reactionEmail;   // who is reacting
}