package com.oath.recommend_domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceEmbeddingRepository extends JpaRepository<PlaceEmbedding, Long> {
    @Query(value = "SELECT * FROM place_embeddings ORDER BY embedding <-> CAST(:queryVector AS vector) LIMIT :limit",
            nativeQuery = true)
    List<PlaceEmbedding> findTopSimilarPlaceEmbeddings(
            @Param("queryVector") float[] queryVector,
            @Param("limit") Long limit
    );

    @Query("SELECT p FROM PlaceEmbedding p WHERE p.placeId = :placeId")
    Optional<PlaceEmbedding> findByPlaceId(@Param("placeId") Long placeId);
}
