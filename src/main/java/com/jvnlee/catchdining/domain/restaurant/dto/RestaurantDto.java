package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.Address;
import com.jvnlee.catchdining.domain.restaurant.model.CountryType;
import com.jvnlee.catchdining.domain.restaurant.model.FoodType;
import com.jvnlee.catchdining.domain.restaurant.model.ServingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {

    private String name;

    private Address address;

    private String phoneNumber;

    private String description;

    private CountryType countryType;

    private FoodType foodType;

    private ServingType servingType;

}
