package com.oath.domain.place_tag_plan.place_tag;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {

    Optional<PlaceTag> findByPlace_IdAndTag_Id(Long placeId, Long tagId);

    boolean existsByPlaceAndTag(Place place, Tag tag);

    Optional<PlaceTag> findByPlace_IdAndTag_Name(Long placeId, String name);

    @Query("select pt from PlaceTag pt join fetch pt.tag")
    List<PlaceTag> findAllWithTags();

}
