package com.oath.domain.place_tag_plan.place;

import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "place_tb")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String address;

    @Column(precision = 10) // scale 속성 제거
    private Double lat;

    @Column(precision = 10) // scale 속성 제거
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceTag> placeTags = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}


