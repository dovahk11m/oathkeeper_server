package com.oath.domain.groups;

import com.oath.domain.chat.ChatRoom;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "groups_tb") // 테이블 이름 컨벤션 통일
@Getter
@NoArgsConstructor
@ToString(exclude = "chatRoom") // 순환 참조 방지
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    // 그룹과 채팅방은 1:1 관계이며, 그룹이 삭제되면 채팅방도 함께 삭제됩니다.
    @OneToOne(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoom chatRoom;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //== 연관관계 편의 메서드 ==//
    /**
     * 그룹 생성 시, 1:1로 매칭되는 채팅방을 설정합니다.
     * @param chatRoom 생성된 채팅방 엔티티
     */
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * DTO로부터 새로운 Group 엔티티를 생성하는 정적 팩토리 메서드입니다.
     * @param request 그룹 생성 요청 DTO
     * @return 생성된 Group 엔티티
     */
    public static Group from(GroupCreateRequest request) {
        return Group.builder()
                .name(request.getGroupName())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
