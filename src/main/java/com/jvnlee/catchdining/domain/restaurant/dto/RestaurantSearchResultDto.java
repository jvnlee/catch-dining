package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.Address;

public interface RestaurantSearchResultDto {

    Long getId();

    String getName();

    Address getAddress();

    double getAvgRating();

    int getReviewCount();

}
