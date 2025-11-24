package com.oath.domain.place_tag_plan.place;

import com.oath.domain.place_tag_plan.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}
