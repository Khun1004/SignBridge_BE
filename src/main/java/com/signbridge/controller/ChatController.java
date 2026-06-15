package com.signbridge.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.ChatMessageDto;
import com.signbridge.entity.ChatMessage;
import com.signbridge.entity.ChatRoom;
import com.signbridge.repository.ChatMessageRepository;
import com.signbridge.repository.ChatRoomRepository;
import com.signbridge.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository messageRepo;
    private final ChatRoomRepository roomRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate broker;

    // GET /api/chat/users?email=xxx
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam("email") String email) {
        List<?> users = userRepo.findByEmailNot(email)
                .stream()
                .map(u -> Map.of(
                        "email", u.getEmail(),
                        "name", u.getName() != null ? u.getName() : u.getEmail(),
                        "orgType", u.getOrgType() != null ? u.getOrgType() : "",
                        "avatar", u.getName() != null && !u.getName().isEmpty()
                                ? String.valueOf(u.getName().charAt(0))
                                : String.valueOf(u.getEmail().charAt(0))))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // GET /api/chat/rooms?email=xxx
    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getRooms(@RequestParam("email") String email) {
        List<ChatRoom> rooms = roomRepo.findByParticipantsContaining(email);
        List<Map<String, Object>> result = rooms.stream().map(r -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", r.getRoomId());
            map.put("roomId", r.getRoomId());
            map.put("isGroup", r.getIsGroup());
            map.put("isOfficial", r.getIsOfficial());
            map.put("participants", r.getParticipants());
            map.put("lastMsg", r.getLastMsg());
            map.put("lastAt", r.getLastAt());
            map.put("memberCount", r.getMemberCount());
            map.put("description", r.getDescription());

            if (!Boolean.TRUE.equals(r.getIsGroup()) && r.getParticipants() != null) {
                String[] parts = r.getParticipants().split(",");
                String otherEmail = java.util.Arrays.stream(parts)
                        .map(String::trim)
                        .filter(e -> !e.equals(email))
                        .findFirst().orElse("");

                if (email.trim().equals(r.getSub())) {
                    String otherName = r.getNameA();
                    if (otherName == null || otherName.isBlank() || otherName.contains("@")) {
                        otherName = otherEmail;
                    }
                    map.put("name", otherName);
                    map.put("avatar", r.getAvatarA() != null ? r.getAvatarA()
                            : (otherName.isEmpty() ? "?" : String.valueOf(otherName.charAt(0))));
                    map.put("sub", email);
                } else {
                    String otherName = r.getName();
                    if (otherName == null || otherName.isBlank() || otherName.contains("@")) {
                        otherName = otherEmail;
                    }
                    map.put("name", otherName);
                    map.put("avatar", r.getAvatar() != null ? r.getAvatar()
                            : (otherName.isEmpty() ? "?" : String.valueOf(otherName.charAt(0))));
                    map.put("sub", r.getSub());
                }
            } else {
                map.put("name", r.getName());
                map.put("avatar", r.getAvatar());
                map.put("sub", r.getSub());
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // POST /api/chat/rooms
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoom room) {
        if (room.getParticipants() != null && !Boolean.TRUE.equals(room.getIsGroup())) {
            String[] parts = room.getParticipants().split(",");
            if (parts.length == 2) {
                Optional<ChatRoom> existing = roomRepo
                        .findByParticipantsContainingAndParticipantsContaining(
                                parts[0].trim(), parts[1].trim());
                if (existing.isPresent())
                    return ResponseEntity.ok(existing.get());
            }
        }
        return ResponseEntity.ok(roomRepo.save(room));
    }

    // POST /api/chat/rooms/direct
    @PostMapping("/rooms/direct")
    public ResponseEntity<ChatRoom> createDirectRoom(@RequestBody Map<String, String> body) {
        String emailA = body.get("emailA");
        String nameA = body.get("nameA");
        String emailB = body.get("emailB");
        String nameB = body.get("nameB");
        String avatarB = body.get("avatarB");

        if (emailA == null || emailB == null)
            return ResponseEntity.badRequest().build();

        if (nameA == null || nameA.isBlank())
            nameA = emailA.split("@")[0];
        if (nameB == null || nameB.isBlank())
            nameB = emailB.split("@")[0];

        Optional<ChatRoom> existing = roomRepo
                .findByParticipantsContainingAndParticipantsContaining(emailA, emailB);
        if (existing.isPresent())
            return ResponseEntity.ok(existing.get());

        String roomId = "room_" + System.currentTimeMillis();
        String avatar = (avatarB != null && !avatarB.isBlank()) ? avatarB : String.valueOf(nameB.charAt(0));
        String avatarA = String.valueOf(nameA.charAt(0));

        ChatRoom room = ChatRoom.builder()
                .roomId(roomId).name(nameB).nameA(nameA).sub(emailB)
                .avatar(avatar).avatarA(avatarA)
                .isGroup(false).isOfficial(false)
                .participants(emailA + "," + emailB)
                .build();

        return ResponseEntity.ok(roomRepo.save(room));
    }

    // GET /api/chat/rooms/{roomId}/messages
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable("roomId") String roomId) {
        List<ChatMessage> msgs = messageRepo.findTop50ByRoomIdOrderBySentAtDesc(roomId);
        Collections.reverse(msgs);
        return ResponseEntity.ok(msgs.stream().map(this::toDto).collect(Collectors.toList()));
    }

    // WebSocket: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDto dto) {
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

        roomRepo.findByRoomId(dto.getRoomId()).ifPresent(room -> {
            room.setLastMsg(dto.getText() != null ? dto.getText()
                    : dto.getFileName() != null ? "📎 " + dto.getFileName() : "");
            room.setLastAt(LocalDateTime.now());
            roomRepo.save(room);
        });

        broker.convertAndSend("/topic/room/" + dto.getRoomId(), toDto(saved));
    }

    // WebSocket: /app/chat.typing — 타이핑 중 알림 브로드캐스트
    @MessageMapping("/chat.typing")
    public void typingNotify(ChatMessageDto dto) {
        ChatMessageDto out = new ChatMessageDto();
        out.setRoomId(dto.getRoomId());
        out.setSenderEmail(dto.getSenderEmail());
        out.setSenderName(dto.getSenderName());
        out.setType("TYPING");
        broker.convertAndSend("/topic/room/" + dto.getRoomId(), out);
    }

    // WebSocket: /app/chat.edit
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

    // WebSocket: /app/chat.delete
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