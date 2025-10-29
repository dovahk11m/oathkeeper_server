//package com.oath.recommend_domain.place;
//
//import com.fasterxml.jackson.annotation.JsonSubTypes;
//import org.hibernate.annotations.Type;
//import org.hibernate.annotations.TypeDef;
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "place_embeddings", schema = "oath")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class place {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "category_id")
//    private Integer categoryId; // PK
//
//    @Column(name = "name", length = 50, nullable = false, unique = true)
//    private String name; // 카테고리 이름 (예: '등산')
//
//    // 선호 키워드 목록 (JSONB -> List<String> 매핑)
//    @Type(type = "jsonb")
//    @Column(name = "pref_keywords", columnDefinition = "jsonb")
//    private List<String> prefKeywords;
//
//    // 비선호 키워드 목록 (JSONB -> List<String> 매핑)
//    @Type(type = "jsonb")
//    @Column(name = "dispref_keywords", columnDefinition = "jsonb")
//    private List<String> disprefKeywords;
//
//    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//    private ZonedDateTime createdAt;
//
//    @PrePersist
//    public void onPrePersist() {
//        this.createdAt = ZonedDateTime.now();
//    }
//}
