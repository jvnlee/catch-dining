package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantCreatedEvent;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantDeletedEvent;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantUpdatedEvent;
import com.jvnlee.catchdining.domain.restaurant.model.Address;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static com.jvnlee.catchdining.domain.restaurant.model.CountryType.일식;
import static com.jvnlee.catchdining.domain.restaurant.model.FoodType.스시;
import static com.jvnlee.catchdining.domain.restaurant.model.ServingType.오마카세;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    RestaurantRepository repository;

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    RestaurantService service;

    @Test
    @DisplayName("식당 등록 성공")
    void register_success() {
        Address address = new Address("서울특별시", "", "강남구", "아무대로", "123");
        RestaurantDto restaurantDto =
                new RestaurantDto("식당", address, "0212345678", "식당 상세정보", 일식, 스시, 오마카세);
        Restaurant restaurant = new Restaurant(restaurantDto);

        when(repository.save(any(Restaurant.class))).thenReturn(restaurant);

        service.register(restaurantDto);

        verify(repository).save(any(Restaurant.class));
        verify(rabbitTemplate).convertAndSend(anyString(), any(RestaurantCreatedEvent.class));
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
        verify(rabbitTemplate).convertAndSend(anyString(), any(RestaurantUpdatedEvent.class));
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
        verify(rabbitTemplate).convertAndSend(anyString(), any(RestaurantDeletedEvent.class));
    }

}