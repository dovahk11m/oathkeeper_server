package com.oath.domain.place_tag_plan.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByName(String name);

    List<Place> findByNameStartingWith(String prefix, Pageable pageable);

    @Query("SELECT p " +
           "FROM Place p " +
           "JOIN p.placeTags pt " +
           "JOIN pt.tag t " +
           "WHERE t.name IN :tagNames " +
           "GROUP BY p.id " +
           "ORDER BY COUNT(p.id) DESC, p.id ASC")
    List<Place> findPlacesByTagsOrderedByMatchCount(
            @Param("tagNames") List<String> tagNames,
            Pageable pageable
    );

    @Query("select distinct p from Place p where p.id in :ids")
    List<Place> findAllById(@Param("ids") List<Long> ids);
}


