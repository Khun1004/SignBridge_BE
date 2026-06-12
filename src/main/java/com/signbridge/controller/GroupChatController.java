package com.signbridge.controller;

import com.signbridge.entity.ChatRoom;
import com.signbridge.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat/groups")
@RequiredArgsConstructor
public class GroupChatController {

    private final ChatRoomRepository roomRepo;

    // ── Get or create official group room ─────────────────
    // Called when user joins a group
    @PostMapping("/join")
    public ResponseEntity<ChatRoom> joinGroup(
            @RequestBody Map<String, String> body) {

        String groupId = body.get("groupId");   // e.g. "official_signbridge"
        String name    = body.get("name");
        String sub     = body.get("sub");
        String avatar  = body.get("avatar");

        // Find existing room or create it
        ChatRoom room = roomRepo.findById(groupId).orElseGet(() -> {
            ChatRoom r = ChatRoom.builder()
                    .roomId(groupId)
                    .name(name)
                    .sub(sub)
                    .avatar(avatar)
                    .isGroup(true)
                    .isOfficial(true)
                    .build();
            return roomRepo.save(r);
        });

        log.info("[Group] joined: {}", groupId);
        return ResponseEntity.ok(room);
    }

    // ── Get all official group rooms ───────────────────────
    @GetMapping("/official")
    public ResponseEntity<List<ChatRoom>> getOfficialRooms() {
        List<ChatRoom> rooms = roomRepo.findByIsOfficialTrue();
        return ResponseEntity.ok(rooms);
    }
}