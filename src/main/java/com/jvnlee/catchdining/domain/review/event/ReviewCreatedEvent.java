package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.domain.review.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCreatedEvent {

    private Long restaurantId;

    private double tasteRating;

    private double moodRating;

    private double serviceRating;

    public ReviewCreatedEvent(Long restaurantId, Review rv) {
        this(restaurantId, rv.getTasteRating(), rv.getMoodRating(), rv.getServiceRating());
    }

}
