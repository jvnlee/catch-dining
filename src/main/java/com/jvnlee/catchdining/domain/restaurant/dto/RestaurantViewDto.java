package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestaurantViewDto {

    private String name;

    private Address address;

    private String phoneNumber;

    private String description;

    private double avgRating;

    private int reviewCount;

    private CountryType countryType;

    private FoodType foodType;

    private ServingType servingType;

    public RestaurantViewDto(RestaurantReviewStat restaurant) {
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.phoneNumber = restaurant.getPhoneNumber();
        this.description = restaurant.getDescription();
        this.avgRating = restaurant.getAvgRating();
        this.reviewCount = restaurant.getReviewCount();
        this.countryType = restaurant.getCountryType();
        this.foodType = restaurant.getFoodType();
        this.servingType = restaurant.getServingType();
    }

}
