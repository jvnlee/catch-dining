package com.jvnlee.catchdining.domain.review.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCreatedEvent {

    private Long reviewId;

}
