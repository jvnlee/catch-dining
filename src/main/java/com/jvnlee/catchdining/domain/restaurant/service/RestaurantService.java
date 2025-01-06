package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public void register(RestaurantDto restaurantDto) {
        validateName(restaurantDto.getName());

        restaurantRepository.save(new Restaurant(restaurantDto));
    }

    @Transactional(readOnly = true)
    public Page<RestaurantSearchResponseDto> search(RestaurantSearchRequestDto restaurantSearchRequestDto) {
        String keyword = restaurantSearchRequestDto.getKeyword();
        SortBy sortBy = restaurantSearchRequestDto.getSortBy();
        Pageable pageable = restaurantSearchRequestDto.getPageable();

        Page<RestaurantSearchResultDto> page;

        if (sortBy.equals(SortBy.AVG_RATING)) {
            page = restaurantRepository.findPageByKeywordSortByAvgRating(keyword, pageable);
        } else if (sortBy.equals(SortBy.REVIEW_COUNT)) {
            page = restaurantRepository.findPageByKeywordSortByReviewCount(keyword, pageable);
        } else {
            page = restaurantRepository.findPageByKeyword(keyword, pageable);
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

    public void update(Long id, RestaurantDto restaurantUpdateDto) {
        validateName(id, restaurantUpdateDto.getName());

        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        restaurant.update(restaurantUpdateDto);
    }

    @Retryable(
            value = OptimisticLockException.class,
            backoff = @Backoff(delay = 100L),
            maxAttempts = 10
    )
    public void updateReviewData(Long restaurantId, double tasteRating, double moodRating, double serviceRating) {
        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        restaurant.updateReviewData(tasteRating, moodRating, serviceRating);
    }

    public void delete(Long id) {
        restaurantRepository.deleteById(id);
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
