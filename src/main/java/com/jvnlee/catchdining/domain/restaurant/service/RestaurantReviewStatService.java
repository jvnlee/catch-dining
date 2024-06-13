package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.annotation.AggregatedData;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.model.RestaurantReviewStat;
import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantReviewStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class RestaurantReviewStatService {

    private final RestaurantReviewStatRepository restaurantReviewStatRepository;

    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void register(Restaurant restaurant) {
        restaurantReviewStatRepository.save(RestaurantReviewStat.from(restaurant));
    }

    @AggregatedData
    @Transactional(readOnly = true)
    public Page<RestaurantSearchResponseDto> search(RestaurantSearchRequestDto restaurantSearchRequestDto) {
        String keyword = restaurantSearchRequestDto.getKeyword();
        SortBy sortBy = restaurantSearchRequestDto.getSortBy();
        Pageable pageable = restaurantSearchRequestDto.getPageable();

        Page<RestaurantSearchResultDto> page;

        if (sortBy.equals(SortBy.AVG_RATING)) {
            page = restaurantReviewStatRepository.findPageByKeywordSortByAvgRating(keyword, pageable);
        } else if (sortBy.equals(SortBy.REVIEW_COUNT)) {
            page = restaurantReviewStatRepository.findPageByKeywordSortByReviewCount(keyword, pageable);
        } else {
            page = restaurantReviewStatRepository.findPageByKeyword(keyword, pageable);
        }

        return page.map(RestaurantSearchResponseDto::new);
    }

    @AggregatedData
    @Transactional(readOnly = true)
    public RestaurantViewDto view(Long id) {
        RestaurantReviewStat restaurant = restaurantReviewStatRepository
                .findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        return new RestaurantViewDto(restaurant);
    }

    @Retryable(
            value = OptimisticLockException.class,
            backoff = @Backoff(delay = 100L),
            maxAttempts = 10
    )
    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void updateReviewData(Long restaurantId, double tasteRating, double moodRating, double serviceRating) {
        RestaurantReviewStat restaurantReviewStat = restaurantReviewStatRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        restaurantReviewStat.updateReviewData(tasteRating, moodRating, serviceRating);
    }

    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void update(Long restaurantId, RestaurantDto restaurantDto) {
        RestaurantReviewStat restaurantReviewStat = restaurantReviewStatRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        restaurantReviewStat.update(restaurantDto);
    }

    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void delete(Long restaurantId) {
        restaurantReviewStatRepository.deleteById(restaurantId);
    }

}
