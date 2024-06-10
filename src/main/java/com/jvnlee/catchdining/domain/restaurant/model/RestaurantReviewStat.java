package com.jvnlee.catchdining.domain.restaurant.model;

import com.jvnlee.catchdining.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
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

    public static RestaurantReviewStat from(Restaurant r) {
        return new RestaurantReviewStat(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getPhoneNumber(),
                r.getDescription(),
                0.0,
                0,
                r.getCountryType(),
                r.getFoodType(),
                r.getServingType()
        );
    }

    public void update(double tasteRating, double moodRating, double serviceRating) {
        double newRating = tasteRating + moodRating + serviceRating / 3;
        double totalRating = avgRating * reviewCount;

        this.reviewCount++;
        this.avgRating = totalRating + newRating / this.reviewCount;
    }

}
