package com.oath.domain.place_tag_plan.place_tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {

    Optional<PlaceTag> findByPlaceIdAndTagId(Long placeId, Long tagId);

}
