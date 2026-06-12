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
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<List<ChatRoom>> getRooms(@RequestParam("email") String email) {
        return ResponseEntity.ok(roomRepo.findByParticipantsContaining(email));
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

        if (emailA == null || emailB == null)
            return ResponseEntity.badRequest().build();

        Optional<ChatRoom> existing = roomRepo
                .findByParticipantsContainingAndParticipantsContaining(emailA, emailB);
        if (existing.isPresent())
            return ResponseEntity.ok(existing.get());

        String roomId = "room_" + System.currentTimeMillis();
        String avatarLetter = nameB != null && !nameB.isEmpty()
                ? String.valueOf(nameB.charAt(0))
                : "?";

        // Store nameA as sub so each side can show the correct other person's name
        // Person A sees nameB (room.name), Person B sees nameA (room.sub)
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .name(nameB != null ? nameB : emailB)
                .sub(nameA != null ? nameA : emailA)
                .avatar(avatarLetter)
                .isGroup(false)
                .isOfficial(false)
                .participants(emailA + "," + emailB)
                .build();

        return ResponseEntity.ok(roomRepo.save(room));
    }

    // GET /api/chat/rooms/{roomId}/messages
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable("roomId") String roomId) {
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

        // Broadcast to room subscribers
        broker.convertAndSend("/topic/room/" + dto.getRoomId(), toDto(saved));

        // Update lastMsg + notify other participants (single DB call)
        roomRepo.findById(dto.getRoomId()).ifPresent(room -> {
            room.setLastMsg(dto.getText() != null ? dto.getText()
                    : dto.getFileName() != null ? "📎 " + dto.getFileName() : "");
            room.setLastAt(LocalDateTime.now());
            roomRepo.save(room);

            if (room.getParticipants() != null) {
                // 1:1 room — notify the other participant
                for (String p : room.getParticipants().split(",")) {
                    String email = p.trim();
                    if (!email.equals(dto.getSenderEmail())) {
                        broker.convertAndSend("/topic/notifications_" + email, toDto(saved));
                    }
                }
            } else if (Boolean.TRUE.equals(room.getIsGroup())) {
                // Group room — broadcast notification to the group topic
                // Each member subscribes to /topic/group_notifications_{roomId}
                broker.convertAndSend("/topic/group_notifications_" + room.getRoomId(), toDto(saved));
            }
        });
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

    // DELETE /api/chat/rooms/{roomId}/messages — delete all messages
    @DeleteMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> deleteRoomMessages(
            @PathVariable String roomId,
            @RequestParam("email") String email) {
        return roomRepo.findById(roomId).map(room -> {
            boolean isParticipant = room.getParticipants() != null
                    && room.getParticipants().contains(email);
            boolean isGroupMember = Boolean.TRUE.equals(room.getIsGroup());
            if (!isParticipant && !isGroupMember) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }
            messageRepo.deleteAllByRoomId(roomId);
            room.setLastMsg("");
            room.setLastAt(null);
            roomRepo.save(room);
            return ResponseEntity.ok(Map.of("message", "메시지 삭제 완료"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // WebSocket: /app/chat.read — tells sender their message was read
    @MessageMapping("/chat.read")
    public void markRead(ChatMessageDto dto) {
        ChatMessageDto out = new ChatMessageDto();
        out.setType("READ");
        out.setRoomId(dto.getRoomId());
        out.setSenderEmail(dto.getSenderEmail());
        broker.convertAndSend("/topic/room/" + dto.getRoomId(), out);
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