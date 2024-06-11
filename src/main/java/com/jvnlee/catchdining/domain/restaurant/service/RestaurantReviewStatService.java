package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.annotation.AggregatedData;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
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
    @Transactional
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
            page = restaurantReviewStatRepository.findPageByKeywordWithSort(keyword, "avgRating", pageable);
        } else if (sortBy.equals(SortBy.REVIEW_COUNT)) {
            page = restaurantReviewStatRepository.findPageByKeywordWithSort(keyword, "reviewCount", pageable);
        } else {
            page = restaurantReviewStatRepository.findPageByKeyword(keyword, pageable);
        }

        return page.map(RestaurantSearchResponseDto::new);
    }

    @Retryable(
            value = OptimisticLockException.class,
            backoff = @Backoff(delay = 100L),
            maxAttempts = 10
    )
    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void update(Restaurant restaurant, double tasteRating, double moodRating, double serviceRating) {
        RestaurantReviewStat restaurantReviewStat = restaurantReviewStatRepository
                .findById(restaurant.getId())
                .orElseThrow(RestaurantNotFoundException::new);

        restaurantReviewStat.update(tasteRating, moodRating, serviceRating);
    }

}
