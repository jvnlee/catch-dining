package com.jvnlee.catchdining.domain.restaurant.model;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "restaurant_review_stat")
public class RestaurantReviewStat extends BaseEntity {

    @Id
    @Column(name = "restaurant_review_stat_id")
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
    private Integer version;

    public RestaurantReviewStat(Long id, RestaurantDto r) {
        this.id = id;
        this.name = r.getName();
        this.address = r.getAddress();
        this.phoneNumber = r.getPhoneNumber();
        this.description = r.getDescription();
        this.countryType = r.getCountryType();
        this.foodType = r.getFoodType();
        this.servingType = r.getServingType();
    }

    public void updateReviewData(double tasteRating, double moodRating, double serviceRating) {
        double newRating = (tasteRating + moodRating + serviceRating) / 3.0;
        double totalRating = this.avgRating * this.reviewCount;

        this.reviewCount++;
        this.avgRating = Math.round((totalRating + newRating) / this.reviewCount * 100.0) / 100.0;
    }

    public void update(RestaurantDto r) {
        this.name = r.getName();
        this.address = r.getAddress();
        this.phoneNumber = r.getPhoneNumber();
        this.description = r.getDescription();
        this.countryType = r.getCountryType();
        this.foodType = r.getFoodType();
        this.servingType = r.getServingType();
    }

}
