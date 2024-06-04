package com.jvnlee.catchdining.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {

    private Long restaurantId;

    private double tasteRating;

    private double moodRating;

    private double serviceRating;

    private String content;

}
