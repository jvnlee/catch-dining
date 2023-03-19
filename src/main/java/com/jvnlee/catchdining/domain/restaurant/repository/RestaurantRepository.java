package com.jvnlee.catchdining.domain.restaurant.repository;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByName(String name);

    @Query("select new com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchDto(r.name, r.address) " +
            "from Restaurant r where r.name like concat('%', :name, '%')")
    Page<RestaurantSearchDto> findPageByName(@Param("name") String name, Pageable pageable);

}
