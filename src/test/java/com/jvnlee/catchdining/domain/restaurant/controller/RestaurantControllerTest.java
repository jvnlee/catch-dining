package com.jvnlee.catchdining.domain.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Address;
import com.jvnlee.catchdining.domain.restaurant.service.RestaurantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.jvnlee.catchdining.domain.restaurant.model.CountryType.*;
import static com.jvnlee.catchdining.domain.restaurant.model.MenuType.*;
import static com.jvnlee.catchdining.domain.restaurant.model.ServingType.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = {RestaurantController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@WithMockUser(username = "user", roles = {"OWNER"})
@MockBean(JpaMetamodelMappingContext.class)
class RestaurantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    RestaurantService service;

    @Test
    @DisplayName("식당 등록 성공")
    void register_success() throws Exception {
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);

        String requestBody = om.writeValueAsString(restaurantDto);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        verify(service).register(restaurantDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 등록 성공"));
    }

    @Test
    @DisplayName("식당 등록 실패: 중복된 상호명")
    void register_fail() throws Exception {
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);

        String requestBody = om.writeValueAsString(restaurantDto);

        doThrow(new DuplicateKeyException("이미 존재하는 상호명입니다.")).when(service).register(any());

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        verify(service).register(restaurantDto);
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 상호명입니다."));
    }

    @Test
    @DisplayName("식당 검색 성공: sort 옵션 없음")
    void search_success_sort_by_none() throws Exception {
        String name = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResponseDto> content = List.of(
                new RestaurantSearchResponseDto(1L, "식당1", address1, 0.0, 0),
                new RestaurantSearchResponseDto(2L, "식당2", address2, 0.0, 0),
                new RestaurantSearchResponseDto(3L, "식당3", address3, 0.0, 0)
        );

        PageImpl<RestaurantSearchResponseDto> page = new PageImpl<>(content);

        RestaurantSearchRequestDto restaurantSearchRequestDto = new RestaurantSearchRequestDto(name, null, pageRequest);
        when(service.search(restaurantSearchRequestDto)).thenReturn(page);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants")
                        .param("keyword", name)
                        .param("page", pageRequest.getPageNumber() + "")
                        .param("size", pageRequest.getPageSize() + "")
        );

        verify(service).search(restaurantSearchRequestDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 검색 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("식당 검색 성공: sort 옵션 rating")
    void search_success_sort_by_rating() throws Exception {
        String name = "식당";
        String sortBy = "rating";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResponseDto> content = List.of(
                new RestaurantSearchResponseDto(1L, "식당1", address1, 5.0, 998),
                new RestaurantSearchResponseDto(2L, "식당2", address2, 4.0, 999),
                new RestaurantSearchResponseDto(3L, "식당3", address3, 3.0, 1000)
        );

        PageImpl<RestaurantSearchResponseDto> page = new PageImpl<>(content);

        RestaurantSearchRequestDto restaurantSearchRequestDto = new RestaurantSearchRequestDto(name, sortBy, pageRequest);
        when(service.search(restaurantSearchRequestDto)).thenReturn(page);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants")
                        .param("keyword", name)
                        .param("sortBy", sortBy)
                        .param("page", pageRequest.getPageNumber() + "")
                        .param("size", pageRequest.getPageSize() + "")
        );

        verify(service).search(restaurantSearchRequestDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 검색 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].rating").value(5.0));
    }

    @Test
    @DisplayName("식당 검색 성공: sort 옵션 reviewCount")
    void search_success_sort_by_review_count() throws Exception {
        String name = "식당";
        String sortBy = "reviewCount";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResponseDto> content = List.of(
                new RestaurantSearchResponseDto(3L, "식당3", address3, 3.0, 1000),
                new RestaurantSearchResponseDto(2L, "식당2", address2, 4.0, 999),
                new RestaurantSearchResponseDto(1L, "식당1", address1, 5.0, 998)
        );

        PageImpl<RestaurantSearchResponseDto> page = new PageImpl<>(content);

        RestaurantSearchRequestDto restaurantSearchRequestDto = new RestaurantSearchRequestDto(name, sortBy, pageRequest);
        when(service.search(restaurantSearchRequestDto)).thenReturn(page);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants")
                        .param("keyword", name)
                        .param("sortBy", sortBy)
                        .param("page", pageRequest.getPageNumber() + "")
                        .param("size", pageRequest.getPageSize() + "")
        );

        verify(service).search(restaurantSearchRequestDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 검색 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].reviewCount").value(1000));
    }

    @Test
    @DisplayName("식당 검색 실패: 결과 없음")
    void search_no_result() throws Exception {
        String name = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        RestaurantSearchRequestDto restaurantSearchRequestDto = new RestaurantSearchRequestDto(name, null, pageRequest);
        doThrow(new RestaurantNotFoundException()).when(service).search(restaurantSearchRequestDto);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants")
                        .param("keyword", name)
                        .param("page", pageRequest.getPageNumber() + "")
                        .param("size", pageRequest.getPageSize() + "")
        );

        verify(service).search(restaurantSearchRequestDto);
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("식당 정보가 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("식당 검색 실패: 유효하지 않은 sort 옵션")
    void search_fail_invalid_sort_by_option() throws Exception {
        String name = "식당";
        String sortBy = "invalidOption";
        PageRequest pageRequest = PageRequest.of(0, 3);

        when(service.search(new RestaurantSearchRequestDto(name, sortBy, pageRequest)))
                .thenThrow(new IllegalArgumentException("유효하지 않은 정렬 파라미터입니다."));

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants")
                        .param("keyword", name)
                        .param("sortBy", sortBy)
                        .param("page", pageRequest.getPageNumber() + "")
                        .param("size", pageRequest.getPageSize() + "")
        );

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 정렬 파라미터입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("식당 정보 조회 성공")
    void view_success() throws Exception {
        String restaurantId = "1";

        when(service.view(Long.valueOf(restaurantId))).thenReturn(new RestaurantViewDto());

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}", restaurantId)
        );

        verify(service).view(Long.valueOf(restaurantId));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 정보 조회 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("식당 정보 조회 실패")
    void view_fail() throws Exception {
        String restaurantId = "1";

        doThrow(new RestaurantNotFoundException()).when(service).view(Long.valueOf(restaurantId));

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}", restaurantId)
        );

        verify(service).view(Long.valueOf(restaurantId));
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("식당 정보가 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("식당 정보 업데이트 성공")
    void update_success() throws Exception {
        String restaurantId = "1";
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);

        String requestBody = om.writeValueAsString(restaurantDto);

        ResultActions resultActions = mockMvc.perform(
                put("/restaurants/{restaurantId}", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        verify(service).update(Long.valueOf(restaurantId), restaurantDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 정보 업데이트 성공"));
    }

    @Test
    @DisplayName("식당 정보 업데이트 실패: 중복된 상호명")
    void update_fail() throws Exception {
        String restaurantId = "1";
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);

        String requestBody = om.writeValueAsString(restaurantDto);

        doThrow(new DuplicateKeyException("이미 존재하는 상호명입니다.")).when(service).update(Long.valueOf(restaurantId), restaurantDto);

        ResultActions resultActions = mockMvc.perform(
                put("/restaurants/{restaurantId}", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        verify(service).update(Long.valueOf(restaurantId), restaurantDto);
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 상호명입니다."));
    }

    @Test
    @DisplayName("식당 삭제 성공")
    void delete_success() throws Exception {
        String restaurantId = "1";

        ResultActions resultActions = mockMvc.perform(
                delete("/restaurants/{restaurantId}", restaurantId)
        );

        verify(service).delete(Long.valueOf(restaurantId));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("식당 삭제 성공"));
    }

    @Test
    @DisplayName("식당 삭제 실패")
    void delete_fail() throws Exception {
        String restaurantId = "1";

        doThrow(new RestaurantNotFoundException()).when(service).delete(Long.valueOf(restaurantId));

        ResultActions resultActions = mockMvc.perform(
                delete("/restaurants/{restaurantId}", restaurantId)
        );

        verify(service).delete(Long.valueOf(restaurantId));
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("식당 정보가 존재하지 않습니다."));
    }

}