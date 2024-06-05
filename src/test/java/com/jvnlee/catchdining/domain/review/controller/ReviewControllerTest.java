package com.jvnlee.catchdining.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = {ReviewController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBean(JpaMetamodelMappingContext.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    ReviewService service;

    @Test
    @DisplayName("리뷰 생성 성공")
    void create_success() throws Exception {
        Long restaurantId = 1L;
        ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(
                restaurantId,
                4.0,
                4.5,
                5.0,
                "Love this place!"
        );

        String requestBody = om.writeValueAsString(reviewCreateRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/reviews", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).create(any());
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 등록 성공"));
    }

    @Test
    @DisplayName("리뷰 생성 실패: 존재하지 않는 식당")
    void create_fail_no_restaurant() throws Exception {
        Long restaurantId = 1L;
        ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(
                restaurantId,
                4.0,
                4.5,
                5.0,
                "Love this place!"
        );

        String requestBody = om.writeValueAsString(reviewCreateRequestDto);

        doThrow(RestaurantNotFoundException.class).when(service).create(any());

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/reviews", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).create(any());
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("식당 정보가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("특정 유저가 작성한 리뷰 조회")
    void viewByUser() throws Exception {
        Long userId = 1L;
        ResultActions resultActions = mockMvc.perform(
                get("/users/{userId}/reviews", userId)
        );

        verify(service).viewByUser(userId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저가 작성한 리뷰 조회 결과"));
    }

    @Test
    @DisplayName("특정 식당에 등록된 리뷰 조회")
    void viewByRestaurant() throws Exception {
        Long restaurantId = 1L;
        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}/reviews", restaurantId)
        );

        verify(service).viewByRestaurant(restaurantId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당에 등록된 리뷰 조회 결과"));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void delete_success() throws Exception {
        Long userId = 1L;
        Long reviewId = 1L;
        ResultActions resultActions = mockMvc.perform(
                delete("/users/{userId}/reviews/{reviewId}", userId, reviewId)
        );

        verify(service).delete(reviewId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 삭제 성공"));
    }

}