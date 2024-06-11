package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantCreatedEvent {

    private Restaurant restaurant;

}
