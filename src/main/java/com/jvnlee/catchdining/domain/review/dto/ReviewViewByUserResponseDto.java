package com.jvnlee.catchdining.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewViewByUserResponseDto {

    private Long reviewId;

    private String restaurantName;

    private LocalDate createdDate;

    private double tasteRating;

    private double moodRating;

    private double serviceRating;

    private String content;

    private int commentCount;

    public ReviewViewByUserResponseDto(ReviewViewByUserResultDto r) {
        this(
                r.getReviewId(),
                r.getRestaurantName(),
                r.getCreatedDate(),
                r.getTasteRating(),
                r.getMoodRating(),
                r.getServiceRating(),
                r.getContent(),
                r.getCommentCount()
        );
    }

}
