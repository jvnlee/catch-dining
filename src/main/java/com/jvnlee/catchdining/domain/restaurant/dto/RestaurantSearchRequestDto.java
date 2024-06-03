package com.jvnlee.catchdining.domain.restaurant.dto;

import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchRequestDto {

    private String keyword;

    private SortBy sortBy;

    private Pageable pageable;

}
