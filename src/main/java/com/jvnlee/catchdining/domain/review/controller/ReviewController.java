package com.jvnlee.catchdining.domain.review.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByRestaurantResponseDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByUserResponseDto;
import com.jvnlee.catchdining.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/restaurants/{restaurantId}/reviews")
    public Response<Void> create(@RequestBody ReviewCreateRequestDto reviewCreateRequestDto) {
        reviewService.create(reviewCreateRequestDto);
        return new Response<>("리뷰 등록 성공");
    }

    @GetMapping("/users/{userId}/reviews")
    public Response<List<ReviewViewByUserResponseDto>> viewByUser(@PathVariable Long userId) {
        List<ReviewViewByUserResponseDto> data = reviewService.viewByUser(userId);
        return new Response<>("유저가 작성한 리뷰 조회 결과", data);
    }

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public Response<List<ReviewViewByRestaurantResponseDto>> viewByRestaurant(@PathVariable Long restaurantId) {
        List<ReviewViewByRestaurantResponseDto> data = reviewService.viewByRestaurant(restaurantId);
        return new Response<>("식당에 등록된 리뷰 조회 결과", data);
    }

    @DeleteMapping("/users/{userId}/reviews/{reviewId}")
    public Response<Void> delete(@PathVariable Long userId, @PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return new Response<>("리뷰 삭제 성공");
    }

}
