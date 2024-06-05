package com.jvnlee.catchdining.domain.review.repository;

import com.jvnlee.catchdining.domain.review.dto.ReviewViewByRestaurantResultDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByUserResultDto;
import com.jvnlee.catchdining.domain.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select rv.id as reviewId, rv.restaurant.name as restaurantName, date(rv.createdDate) as createdDate, " +
            "rv.tasteRating as tasteRating, rv.moodRating as moodRating, rv.serviceRating as serviceRating, " +
            "rv.content as content, count(rvc.id) as commentCount " +
            "from Review rv " +
            "left join rv.reviewComments rvc " +
            "where rv.user.id = :userId " +
            "group by rv.id")
    List<ReviewViewByUserResultDto> findAllByUserId(Long userId);

    @Query("select rv.id as reviewId, rv.user.username as username, date(rv.createdDate) as createdDate, " +
            "rv.tasteRating as tasteRating, rv.moodRating as moodRating, rv.serviceRating as serviceRating, " +
            "rv.content as content, count(rvc.id) as commentCount " +
            "from Review rv " +
            "left join rv.reviewComments rvc " +
            "where rv.restaurant.id = :restaurantId " +
            "group by rv.id")
    List<ReviewViewByRestaurantResultDto> findAllByRestaurantId(Long restaurantId);

}
