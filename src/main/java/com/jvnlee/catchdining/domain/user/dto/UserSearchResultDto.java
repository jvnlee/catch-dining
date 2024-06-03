package com.jvnlee.catchdining.domain.user.dto;

import com.jvnlee.catchdining.undeveloped.Favorite;
import com.jvnlee.catchdining.domain.review.model.Review;

import java.util.List;

public interface UserSearchResultDto {

    Long getId();

    String getUsername();

    List<Favorite> getFavorites();

    List<Review> getReviews();

}
