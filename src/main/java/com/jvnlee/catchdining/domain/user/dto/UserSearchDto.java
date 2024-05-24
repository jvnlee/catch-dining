package com.jvnlee.catchdining.domain.user.dto;

import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.entity.Favorite;
import com.jvnlee.catchdining.entity.Review;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserSearchDto {

    private String username;

    private List<Favorite> favorites;

    private List<Review> reviews;

    public UserSearchDto(User user) {
        this.username = user.getUsername();
        this.favorites = user.getFavorites();
        this.reviews = user.getReviews();
    }

}
