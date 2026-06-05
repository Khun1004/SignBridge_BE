package com.signbridge.controller;

import com.signbridge.dto.ChatMessageDto;
import com.signbridge.entity.ChatMessage;
import com.signbridge.entity.ChatRoom;
import com.signbridge.repository.ChatMessageRepository;
import com.signbridge.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository messageRepo;
    private final ChatRoomRepository    roomRepo;
    private final SimpMessagingTemplate broker;     // sends to /topic/...

    // ----------------------------------------------------------
    // WebSocket: client sends to /app/chat.send
    // Server broadcasts to /topic/room/{roomId}
    // ----------------------------------------------------------
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDto dto) {

        // 1. Persist to MySQL
        ChatMessage saved = messageRepo.save(ChatMessage.builder()
                .roomId(dto.getRoomId())
                .senderEmail(dto.getSenderEmail())
                .senderName(dto.getSenderName())
                .text(dto.getText())
                .fileName(dto.getFileName())
                .fileUrl(dto.getFileUrl())
                .isImage(Boolean.TRUE.equals(dto.getIsImage()))
                .replyToId(dto.getReplyToId())
                .replyToName(dto.getReplyToName())
                .replyToText(dto.getReplyToText())
                .forwardedFrom(dto.getForwardedFrom())
                .isSystem(Boolean.TRUE.equals(dto.getIsSystem()))
                .build());

        // 2. Update room's lastMsg
        roomRepo.findById(dto.getRoomId()).ifPresent(room -> {
            room.setLastMsg(dto.getText() != null ? dto.getText()
                    : dto.getFileName() != null ? "📎 " + dto.getFileName() : "");
            room.setLastAt(LocalDateTime.now());
            roomRepo.save(room);
        });

        // 3. Broadcast to all subscribers of this room
        broker.convertAndSend(
                "/topic/room/" + dto.getRoomId(),
                toDto(saved)
        );
    }

    // ----------------------------------------------------------
    // WebSocket: client sends to /app/chat.edit
    // ----------------------------------------------------------
    @MessageMapping("/chat.edit")
    public void editMessage(ChatMessageDto dto) {
        messageRepo.findById(dto.getId()).ifPresent(msg -> {
            msg.setText(dto.getText());
            msg.setIsEdited(true);
            ChatMessage saved = messageRepo.save(msg);
            ChatMessageDto out = toDto(saved);
            out.setType("EDIT");
            broker.convertAndSend("/topic/room/" + msg.getRoomId(), out);
        });
    }

    // ----------------------------------------------------------
    // WebSocket: client sends to /app/chat.delete
    // ----------------------------------------------------------
    @MessageMapping("/chat.delete")
    public void deleteMessage(ChatMessageDto dto) {
        messageRepo.findById(dto.getId()).ifPresent(msg -> {
            String roomId = msg.getRoomId();
            messageRepo.delete(msg);
            ChatMessageDto out = new ChatMessageDto();
            out.setId(dto.getId());
            out.setRoomId(roomId);
            out.setType("DELETE");
            broker.convertAndSend("/topic/room/" + roomId, out);
        });
    }

    // ----------------------------------------------------------
    // REST: GET /api/chat/rooms?email=xxx
    // Returns all rooms for a user
    // ----------------------------------------------------------
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getRooms(@RequestParam String email) {
        return ResponseEntity.ok(roomRepo.findByParticipantsContaining(email));
    }

    // ----------------------------------------------------------
    // REST: POST /api/chat/rooms
    // Creates a new 1:1 room (called when starting a chat with a friend)
    // ----------------------------------------------------------
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoom room) {
        // Avoid duplicates for 1:1 chats
        if (room.getParticipants() != null && !Boolean.TRUE.equals(room.getIsGroup())) {
            String[] parts = room.getParticipants().split(",");
            if (parts.length == 2) {
                Optional<ChatRoom> existing = roomRepo
                        .findByParticipantsContainingAndParticipantsContaining(
                                parts[0].trim(), parts[1].trim());
                if (existing.isPresent()) return ResponseEntity.ok(existing.get());
            }
        }
        return ResponseEntity.ok(roomRepo.save(room));
    }

    // ----------------------------------------------------------
    // REST: GET /api/chat/rooms/{roomId}/messages
    // Returns last 50 messages when opening a room
    // ----------------------------------------------------------
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable String roomId) {
        List<ChatMessage> msgs = messageRepo
                .findTop50ByRoomIdOrderBySentAtDesc(roomId);
        // Reverse so oldest is first
        Collections.reverse(msgs);
        return ResponseEntity.ok(msgs.stream().map(this::toDto).collect(Collectors.toList()));
    }

    // ----------------------------------------------------------
    // Helper: entity → DTO
    // ----------------------------------------------------------
    private ChatMessageDto toDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .roomId(m.getRoomId())
                .senderEmail(m.getSenderEmail())
                .senderName(m.getSenderName())
                .text(m.getText())
                .fileName(m.getFileName())
                .fileUrl(m.getFileUrl())
                .isImage(m.getIsImage())
                .replyToId(m.getReplyToId())
                .replyToName(m.getReplyToName())
                .replyToText(m.getReplyToText())
                .forwardedFrom(m.getForwardedFrom())
                .isSystem(m.getIsSystem())
                .isEdited(m.getIsEdited())
                .sentAt(m.getSentAt() != null
                        ? m.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : null)
                .type("SEND")
                .build();
    }
}