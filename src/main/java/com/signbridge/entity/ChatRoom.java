package com.signbridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    // ✅ roomId를 PK로 — DB에 id 컬럼 없어도 동작
    @Id
    @Column(name = "room_id", unique = true)
    private String roomId;

    @Column(name = "name")
    private String name;

    @Column(name = "name_a")
    private String nameA;

    @Column(name = "sub")
    private String sub;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "avatar_a")
    private String avatarA;

    @Column(name = "is_group")
    private Boolean isGroup;

    @Column(name = "is_official")
    private Boolean isOfficial;

    @Column(name = "participants", length = 500)
    private String participants;

    @Column(name = "last_msg", length = 1000)
    private String lastMsg;

    @Column(name = "last_at")
    private java.time.LocalDateTime lastAt;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}