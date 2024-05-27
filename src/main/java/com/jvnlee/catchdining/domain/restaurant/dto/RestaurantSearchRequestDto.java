package com.jvnlee.catchdining.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchRequestDto {

    private String keyword;

    private String sort;

    private Pageable pageable;

}
