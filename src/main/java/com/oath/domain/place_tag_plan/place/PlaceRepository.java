package com.oath.domain.place_tag_plan.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("select distinct p from Place p where p.id in :ids")
    List<Place> findAllById(@Param("ids") List<Long> ids);
}
