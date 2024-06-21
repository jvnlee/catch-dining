package com.jvnlee.catchdining.domain.menu.repository;

import com.jvnlee.catchdining.domain.menu.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByName(String name);

    @Query("select m from Menu m where m.restaurant.id = :restaurantId")
    List<Menu> findAllByRestaurantId(Long restaurantId);

}
