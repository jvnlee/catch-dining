package com.jvnlee.catchdining.domain.restaurant.model;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Restaurant extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(unique = true)
    private String name;

    @Embedded
    private Address address;

    private String phoneNumber;

    private String description;

    private double rating;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant")
    private List<Seat> seats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CountryType countryType;

    @Enumerated(EnumType.STRING)
    private FoodType foodType;

    @Enumerated(EnumType.STRING)
    private ServingType servingType;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant")
    private List<Menu> menus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant")
    private List<Review> reviews = new ArrayList<>();

    public Restaurant(RestaurantDto restaurantDto) {
        this.name = restaurantDto.getName();
        this.address = restaurantDto.getAddress();
        this.phoneNumber = restaurantDto.getPhoneNumber();
        this.description = restaurantDto.getDescription();
        this.countryType = restaurantDto.getCountryType();
        this.foodType = restaurantDto.getFoodType();
        this.servingType = restaurantDto.getServingType();
    }

    public void update(RestaurantDto restaurantDto) {
        this.name = restaurantDto.getName();
        this.address = restaurantDto.getAddress();
        this.phoneNumber = restaurantDto.getPhoneNumber();
        this.description = restaurantDto.getDescription();
        this.countryType = restaurantDto.getCountryType();
        this.foodType = restaurantDto.getFoodType();
        this.servingType = restaurantDto.getServingType();
    }
}
