package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.review.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCreatedEvent {

    private Restaurant restaurant;

    private double tasteRating;

    private double moodRating;

    private double serviceRating;

    public ReviewCreatedEvent(Review rv) {
        this(rv.getRestaurant(), rv.getTasteRating(), rv.getMoodRating(), rv.getServiceRating());
    }

}
