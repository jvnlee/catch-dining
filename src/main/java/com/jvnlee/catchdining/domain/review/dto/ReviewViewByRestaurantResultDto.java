package com.jvnlee.catchdining.domain.review.dto;

import java.time.LocalDate;

public interface ReviewViewByRestaurantResultDto {

    Long getReviewId();

    String getUsername();

    LocalDate getCreatedDate();

    double getTasteRating();

    double getMoodRating();

    double getServiceRating();

    String getContent();

    int getCommentCount();

}
