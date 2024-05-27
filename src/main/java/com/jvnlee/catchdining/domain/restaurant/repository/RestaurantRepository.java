package com.jvnlee.catchdining.domain.restaurant.repository;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByName(String name);

    @Query(
            "select r.id as id, r.name as name, r.address as address, (avg(rv.tasteRating) + avg(rv.moodRating) + avg(rv.serviceRating)) / 3 as rating, count(rv.id) as reviewCount " +
            "from Restaurant r " +
            "left join r.reviews rv " +
            "where r.name like concat('%', :keyword, '%') " +
            "group by r.id"
    )
    Page<RestaurantSearchResponseDto> findPageByKeyword(String keyword, Pageable pageable);

    @Query(
            "select r.id as id, r.name as name, r.address as address, round((avg(rv.tasteRating) + avg(rv.moodRating) + avg(rv.serviceRating)) / 3, 2) as rating, count(rv.id) as reviewCount " +
            "from Restaurant r " +
            "left join r.reviews rv " +
            "where r.name like concat('%', :keyword, '%') " +
            "group by r.id " +
            "order by (avg(rv.tasteRating) + avg(rv.moodRating) + avg(rv.serviceRating)) / 3 desc"
    )
    Page<RestaurantSearchResponseDto> findPageByKeywordOrderByRating(String keyword, Pageable pageable);

    @Query(
            "select r.id as id, r.name as name, r.address as address, round((avg(rv.tasteRating) + avg(rv.moodRating) + avg(rv.serviceRating)) / 3, 2) as rating, count(rv.id) as reviewCount " +
            "from Restaurant r " +
            "left join r.reviews rv " +
            "where r.name like concat('%', :keyword, '%') " +
            "group by r.id " +
            "order by count(rv.id) desc"
    )
    Page<RestaurantSearchResponseDto> findPageByKeywordOrderByReviewCount(String keyword, Pageable pageable);

}
