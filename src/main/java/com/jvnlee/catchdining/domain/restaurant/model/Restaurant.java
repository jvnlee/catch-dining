package com.jvnlee.catchdining.domain.restaurant.model;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.domain.menu.model.Menu;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Table(name = "restaurant")
public class Restaurant extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(unique = true)
    private String name;

    @Embedded
    private Address address;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String description;

    @Column(name = "avg_rating")
    private double avgRating;

    @Column(name = "review_count")
    private int reviewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "country_type")
    private CountryType countryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type")
    private FoodType foodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "serving_type")
    private ServingType servingType;

    @Version
    private int version;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = ALL, orphanRemoval = true)
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

    public void update(RestaurantDto restaurantUpdateDto) {
        this.name = restaurantUpdateDto.getName();
        this.address = restaurantUpdateDto.getAddress();
        this.phoneNumber = restaurantUpdateDto.getPhoneNumber();
        this.description = restaurantUpdateDto.getDescription();
        this.countryType = restaurantUpdateDto.getCountryType();
        this.foodType = restaurantUpdateDto.getFoodType();
        this.servingType = restaurantUpdateDto.getServingType();
    }

    public void updateReviewData(double tasteRating, double moodRating, double serviceRating) {
        double newRating = (tasteRating + moodRating + serviceRating) / 3.0;
        double totalRating = this.avgRating * this.reviewCount;

        this.reviewCount++;
        this.avgRating = Math.round((totalRating + newRating) / this.reviewCount * 100.0) / 100.0;
    }

}
