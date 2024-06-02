package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public void register(RestaurantDto restaurantDto) {
        validateName(restaurantDto.getName());
        Restaurant restaurant = new Restaurant(restaurantDto);
        restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantSearchResponseDto> search(RestaurantSearchRequestDto restaurantSearchRequestDto) {
        String keyword = restaurantSearchRequestDto.getKeyword();
        SortBy sortBy = restaurantSearchRequestDto.getSortBy();
        Pageable pageable = restaurantSearchRequestDto.getPageable();

        Page<RestaurantSearchResultDto> page;

        if (sortBy == null) {
            page = restaurantRepository.findPageByKeyword(keyword, pageable);
        } else {
            if (sortBy.equals(SortBy.RATING)) {
//                page = restaurantRepository.findPageByKeywordOrderByRating(keyword, pageable);
                page = restaurantRepository.findPageByKeywordOrderByRatingWithSubQuery(keyword, pageable);
            } else if (sortBy.equals(SortBy.REVIEW_COUNT)) {
                page = restaurantRepository.findPageByKeywordOrderByReviewCount(keyword, pageable);
            } else {
                throw new IllegalArgumentException("유효하지 않은 정렬 파라미터입니다.");
            }
        }

        if (page.getTotalElements() == 0) {
            throw new RestaurantNotFoundException();
        }

        return page.map(RestaurantSearchResponseDto::new);
    }

    @Transactional(readOnly = true)
    public RestaurantViewDto view(Long id) {
        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        return new RestaurantViewDto(restaurant);
    }

    public void update(Long id, RestaurantDto restaurantDto) {
        validateName(id, restaurantDto.getName());
        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        restaurant.update(restaurantDto);
    }

    public void delete(Long id) {
        try {
            restaurantRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RestaurantNotFoundException();
        }
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
