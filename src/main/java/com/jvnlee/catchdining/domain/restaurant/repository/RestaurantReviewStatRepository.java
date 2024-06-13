package com.jvnlee.catchdining.domain.restaurant.repository;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.model.RestaurantReviewStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantReviewStatRepository extends JpaRepository<RestaurantReviewStat, Long> {

    @Query("select r from RestaurantReviewStat r where r.name like concat('%', :keyword, '%') order by r.avgRating desc")
    Page<RestaurantSearchResultDto> findPageByKeywordSortByAvgRating(String keyword, Pageable pageable);

    @Query("select r from RestaurantReviewStat r where r.name like concat('%', :keyword, '%') order by r.reviewCount desc")
    Page<RestaurantSearchResultDto> findPageByKeywordSortByReviewCount(String keyword, Pageable pageable);

    @Query("select r from RestaurantReviewStat r where r.name like concat('%', :keyword, '%')")
    Page<RestaurantSearchResultDto> findPageByKeyword(String keyword, Pageable pageable);

}
