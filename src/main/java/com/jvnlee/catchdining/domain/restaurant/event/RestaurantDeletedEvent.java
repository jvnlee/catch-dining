package com.jvnlee.catchdining.domain.restaurant.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDeletedEvent {

    private Long restaurantId;

}
