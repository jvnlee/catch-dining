package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Address;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.jvnlee.catchdining.domain.restaurant.model.CountryType.일식;
import static com.jvnlee.catchdining.domain.restaurant.model.FoodType.스시;
import static com.jvnlee.catchdining.domain.restaurant.model.ServingType.오마카세;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    RestaurantRepository repository;

    @InjectMocks
    RestaurantService service;

    @Test
    @DisplayName("식당 등록 성공")
    void register_success() {
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);

        service.register(restaurantDto);

        verify(repository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("식당 등록 실패: 중복된 상호명")
    void register_fail() {
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);
        Restaurant restaurant = new Restaurant(restaurantDto);

        when(repository.findByName(restaurantDto.getName())).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> service.register(restaurantDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("식당 검색 성공")
    void search_success() {
        String name = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchDto> content = List.of(
                new RestaurantSearchDto("식당1", address1),
                new RestaurantSearchDto("식당2", address2),
                new RestaurantSearchDto("식당3", address3)
        );

        PageImpl<RestaurantSearchDto> page = new PageImpl<>(content);

        when(repository.findPageByName(name, pageRequest)).thenReturn(page);

        assertThat(service.search(name, pageRequest).getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("식당 검색 실패: 결과 없음")
    void search_fail() {
        String name = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        List<RestaurantSearchDto> content = Collections.emptyList();

        PageImpl<RestaurantSearchDto> page = new PageImpl<>(content);

        when(repository.findPageByName(name, pageRequest)).thenReturn(page);

        assertThatThrownBy(() -> service.search(name, pageRequest))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    @DisplayName("식당 정보 조회 성공")
    void view_success() {
        Long restaurantId = 1L;
        Restaurant restaurant = Restaurant.builder()
                .name("식당")
                .build();

        when(repository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        RestaurantViewDto restaurantViewDto = service.view(restaurantId);

        assertThat(restaurantViewDto.getName()).isEqualTo(restaurant.getName());
    }

    @Test
    @DisplayName("식당 정보 조회 실패")
    void view_fail() {
        Long restaurantId = 1L;

        when(repository.findById(restaurantId)).thenThrow(RestaurantNotFoundException.class);

        assertThatThrownBy(() -> service.view(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    @DisplayName("식당 정보 업데이트 성공")
    void update_success() {
        Long restaurantId = 1L;
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);
        Restaurant restaurant = mock(Restaurant.class);

        when(repository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        service.update(restaurantId, restaurantDto);

        verify(restaurant).update(restaurantDto);
    }

    @Test
    @DisplayName("식당 정보 업데이트 실패: 중복된 상호명")
    void update_fail() {
        Long restaurantId = 1L;
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);
        Restaurant restaurantMock = mock(Restaurant.class);
        Optional<Restaurant> restaurant = Optional.of(restaurantMock);

        when(repository.findByName(restaurantDto.getName())).thenReturn(restaurant);
        when(restaurant.get().getId()).thenReturn(2L);

        assertThatThrownBy(() -> service.update(restaurantId, restaurantDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("식당 정보 삭제 성공")
    void delete_success() {
        Long restaurantId = 1L;

        service.delete(restaurantId);

        verify(repository).deleteById(restaurantId);
    }

    @Test
    @DisplayName("식당 정보 삭제 실패")
    void delete_fail() {
        Long restaurantId = 1L;

        doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(restaurantId);

        assertThatThrownBy(() -> service.delete(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class);
        verify(repository).deleteById(restaurantId);
    }

}