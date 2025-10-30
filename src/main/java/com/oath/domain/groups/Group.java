package com.oath.domain.groups;

import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_tb") // 테이블 이름 컨벤션 통일
@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
