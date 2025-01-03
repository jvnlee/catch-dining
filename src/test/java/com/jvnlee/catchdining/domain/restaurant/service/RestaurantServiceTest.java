package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResponseDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantSearchResultDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantViewDto;
import com.jvnlee.catchdining.domain.restaurant.model.Address;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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

    @InjectMocks
    RestaurantService service;

    class RestaurantSearchResultDtoImpl implements RestaurantSearchResultDto {
        private Long id;
        private String name;
        private Address address;
        private double avgRating;
        private int reviewCount;

        public RestaurantSearchResultDtoImpl(Long id, String name, Address address, double avgRating, int reviewCount) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.avgRating = avgRating;
            this.reviewCount = reviewCount;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Address getAddress() {
            return address;
        }

        @Override
        public double getAvgRating() {
            return avgRating;
        }

        @Override
        public int getReviewCount() {
            return reviewCount;
        }
    }

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
    @DisplayName("식당 검색 성공: sort 옵션 없음")
    void search_success() {
        String keyword = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResultDto> content = List.of(
                new RestaurantSearchResultDtoImpl(1L, "식당1", address1, 5.0, 999),
                new RestaurantSearchResultDtoImpl(2L, "식당2", address2, 3.0, 1000),
                new RestaurantSearchResultDtoImpl(3L, "식당3", address3, 4.0, 998)
        );

        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(content);

        when(repository.findPageByKeyword(keyword, pageRequest)).thenReturn(page);

        assertThat(service.search(new RestaurantSearchRequestDto(keyword, SortBy.NONE, pageRequest)).getContent())
                .hasOnlyElementsOfType(RestaurantSearchResponseDto.class)
                .hasSize(3);
        verify(repository).findPageByKeyword(keyword, pageRequest);
    }

    @Test
    @DisplayName("식당 검색 성공: sort 옵션 avgRating")
    void search_success_sort_by_rating() {
        String keyword = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResultDto> content = List.of(
                new RestaurantSearchResultDtoImpl(1L, "식당1", address1, 5.0, 998),
                new RestaurantSearchResultDtoImpl(2L, "식당2", address2, 4.0, 999),
                new RestaurantSearchResultDtoImpl(3L, "식당3", address3, 3.0, 1000)
        );

        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(content);

        when(repository.findPageByKeywordSortByAvgRating(keyword, pageRequest)).thenReturn(page);

        assertThat(service.search(new RestaurantSearchRequestDto(keyword, SortBy.AVG_RATING, pageRequest)).getContent())
                .hasOnlyElementsOfType(RestaurantSearchResponseDto.class)
                .hasSize(3);
        verify(repository).findPageByKeywordSortByAvgRating(keyword, pageRequest);
    }

    @Test
    @DisplayName("식당 검색 성공: sort 옵션 reviewCount")
    void search_success_sort_by_review_count() {
        String keyword = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        Address address1 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address2 = new Address("서울특별시", "", "강남구", "아무대로", "123");
        Address address3 = new Address("서울특별시", "", "강남구", "아무대로", "123");

        List<RestaurantSearchResultDto> content = List.of(
                new RestaurantSearchResultDtoImpl(3L, "식당3", address3, 3.0, 1000),
                new RestaurantSearchResultDtoImpl(2L, "식당2", address2, 4.0, 999),
                new RestaurantSearchResultDtoImpl(1L, "식당1", address1, 5.0, 998)
        );

        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(content);

        when(repository.findPageByKeywordSortByReviewCount(keyword, pageRequest)).thenReturn(page);

        assertThat(service.search(new RestaurantSearchRequestDto(keyword, SortBy.REVIEW_COUNT, pageRequest)).getContent())
                .hasOnlyElementsOfType(RestaurantSearchResponseDto.class)
                .hasSize(3);
        verify(repository).findPageByKeywordSortByReviewCount(keyword, pageRequest);
    }

    @Test
    @DisplayName("식당 검색 실패: 결과 없음")
    void search_fail_no_result() {
        String keyword = "식당";
        PageRequest pageRequest = PageRequest.of(0, 3);

        when(repository.findPageByKeyword(keyword, pageRequest)).thenReturn(Page.empty());

        assertThat(service.search(new RestaurantSearchRequestDto(keyword, SortBy.NONE, pageRequest)).getContent())
                .hasOnlyElementsOfType(RestaurantSearchResponseDto.class)
                .isEmpty();
        verify(repository).findPageByKeyword(keyword, pageRequest);
    }

    @Test
    @DisplayName("식당 정보 조회 성공")
    void view_success() {
        Long restaurantId = 1L;
        Restaurant restaurant = Restaurant.builder().name("restaurant").build();

        when(repository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        RestaurantViewDto restaurantViewDto = service.view(restaurantId);

        assertThat(restaurantViewDto.getName()).isEqualTo(restaurant.getName());
    }

    @Test
    @DisplayName("식당 정보 조회 실패")
    void view_fail() {
        Long restaurantId = 1L;

        when(repository.findById(restaurantId)).thenReturn(Optional.empty());

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

}