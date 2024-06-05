package com.jvnlee.catchdining.domain.review.dto;

import java.time.LocalDate;

public interface ReviewViewByUserResultDto {

    Long getReviewId();

    String getRestaurantName();

    LocalDate getCreatedDate();

    double getTasteRating();

    double getMoodRating();

    double getServiceRating();

    String getContent();

    int getCommentCount();

}
