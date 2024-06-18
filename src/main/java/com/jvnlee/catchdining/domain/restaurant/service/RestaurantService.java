package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantCreateResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantCreatedEvent;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantDeletedEvent;
import com.jvnlee.catchdining.domain.restaurant.event.RestaurantUpdatedEvent;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    private final ApplicationEventPublisher eventPublisher;

    public RestaurantCreateResponseDto register(RestaurantDto restaurantDto) {
        validateName(restaurantDto.getName());
        Restaurant restaurant = new Restaurant(restaurantDto);
        Restaurant saved = restaurantRepository.save(restaurant);
        eventPublisher.publishEvent(new RestaurantCreatedEvent(restaurant));
        return new RestaurantCreateResponseDto(saved.getId());
    }

    public void update(Long id, RestaurantDto restaurantUpdateDto) {
        validateName(id, restaurantUpdateDto.getName());
        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        restaurant.update(restaurantUpdateDto);
        eventPublisher.publishEvent(new RestaurantUpdatedEvent(id, restaurantUpdateDto));
    }

    public void delete(Long id) {
        restaurantRepository.deleteById(id);
        eventPublisher.publishEvent(new RestaurantDeletedEvent(id));
    }

    private void validateName(String name) {
        if (restaurantRepository.findByName(name).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 상호명입니다.");
        }
    }

    private void validateName(Long id, String name) {
        Optional<Restaurant> restaurant = restaurantRepository.findByName(name);
        if (restaurant.isPresent()) {
            if (restaurant.get().getId().equals(id)) return;
            throw new DuplicateKeyException("이미 존재하는 상호명입니다.");
        }
    }

}
