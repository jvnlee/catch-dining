package com.jvnlee.catchdining.domain.restaurant.repository;

import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByName(String name);

}
