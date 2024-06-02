package com.jvnlee.catchdining.domain.restaurant.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import com.jvnlee.catchdining.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> register(@RequestBody RestaurantDto restaurantDto) {
        restaurantService.register(restaurantDto);
        return new Response<>("식당 등록 성공");
    }

    @GetMapping
    public Response<Page<RestaurantSearchResponseDto>> search(@RequestParam String keyword,
                                                            @RequestParam(required = false) SortBy sortBy,
                                                            Pageable pageable) {
        Page<RestaurantSearchResponseDto> data = restaurantService
                .search(new RestaurantSearchRequestDto(keyword, sortBy, pageable));
        return new Response<>("식당 검색 결과", data);
    }

    @GetMapping("/{restaurantId}")
    public Response<RestaurantViewDto> view(@PathVariable Long restaurantId) {
        RestaurantViewDto data = restaurantService.view(restaurantId);
        return new Response<>("식당 정보 조회 결과", data);
    }

    @PutMapping("/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> update(@PathVariable Long restaurantId, @RequestBody RestaurantDto restaurantDto) {
        restaurantService.update(restaurantId, restaurantDto);
        return new Response<>("식당 정보 업데이트 성공");
    }

    @DeleteMapping("/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> delete(@PathVariable Long restaurantId) {
        restaurantService.delete(restaurantId);
        return new Response<>("식당 삭제 성공");
    }

}
