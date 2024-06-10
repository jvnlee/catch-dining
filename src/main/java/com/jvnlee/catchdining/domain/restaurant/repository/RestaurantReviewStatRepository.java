package com.jvnlee.catchdining.domain.restaurant.repository;

import com.jvnlee.catchdining.domain.restaurant.model.RestaurantReviewStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantReviewStatRepository extends JpaRepository<RestaurantReviewStat, Long> {
}
