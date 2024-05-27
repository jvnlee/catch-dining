package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.Address;

public interface RestaurantSearchResponseDto {

    Long getId();

    String getName();

    Address getAddress();

    double getRating();

    int getReviewCount();

}
