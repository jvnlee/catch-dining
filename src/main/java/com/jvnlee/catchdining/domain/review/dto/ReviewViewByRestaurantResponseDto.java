package com.jvnlee.catchdining.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewViewByRestaurantResponseDto {

    private Long reviewId;

    private String username;

    private LocalDate createdDate;

    private double tasteRating;

    private double moodRating;

    private double serviceRating;

    private String content;

    private int commentCount;

    public ReviewViewByRestaurantResponseDto(ReviewViewByRestaurantResultDto r) {
        this(
                r.getReviewId(),
                r.getUsername(),
                r.getCreatedDate(),
                r.getTasteRating(),
                r.getMoodRating(),
                r.getServiceRating(),
                r.getContent(),
                r.getCommentCount()
        );
    }

}