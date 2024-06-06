package com.jvnlee.catchdining.domain.user.dto;

import com.jvnlee.catchdining.undeveloped.Favorite;
import com.jvnlee.catchdining.domain.review.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponseDto {

    private String username;

    private List<Favorite> favorites;

    private List<Review> reviews;

    public UserSearchResponseDto(UserSearchResultDto u) {
        this(u.getUsername(), u.getFavorites(), u.getReviews());
    }

}
