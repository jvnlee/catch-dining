package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchResponseDto {

    private Long id;

    private String name;

    private Address address;

    private double rating;

    private int reviewCount;

    public RestaurantSearchResponseDto(RestaurantSearchResultDto r) {
        this(r.getId(), r.getName(), r.getAddress(), r.getRating(), r.getReviewCount());
    }

}
