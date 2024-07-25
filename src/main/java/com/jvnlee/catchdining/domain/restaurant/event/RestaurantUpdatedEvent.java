package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUpdatedEvent {

    private Long restaurantId;

    private RestaurantDto restaurantDto;

}
