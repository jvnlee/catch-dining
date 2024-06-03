package com.jvnlee.catchdining.entity;

import lombok.Getter;
import org.hibernate.annotations.Subselect;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Entity
@Subselect(
        "select rv.restaurant_id as restaurant_id, round((avg(rv.taste_rating) + avg(rv.mood_rating) + avg(rv.service_rating)) / 3, 2) as rating, count(rv.review_id) as review_count " +
        "from Review rv " +
        "group by rv.restaurant_id"
)
public class ReviewStats {

    @Id
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "rating")
    private double rating;

    @Column(name = "review_count")
    private int reviewCount;

}
