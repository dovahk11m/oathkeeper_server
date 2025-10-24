package com.oath.domain.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationPoint {

    @Column(name = "place_latitude")
    private Double latitude;

    @Column(name = "place_longitude")
    private Double longitude;

}
