package com.jvnlee.catchdining.domain.restaurant.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantDeletedEvent {

    private Long restaurantId;

}
