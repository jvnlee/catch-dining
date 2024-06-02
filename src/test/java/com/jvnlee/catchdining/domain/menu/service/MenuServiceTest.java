package com.jvnlee.catchdining.domain.menu.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.menu.model.Menu;
import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.dto.MenuViewDto;
import com.jvnlee.catchdining.domain.menu.repository.MenuRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    RestaurantRepository restaurantRepository;

    @InjectMocks
    MenuService menuService;

    @Test
    @DisplayName("메뉴 등록 성공")
    void add_success() {
        Long restaurantId = 1L;

        List<MenuDto> menuDtoList = List.of(
                new MenuDto("런치 오마카세", 80_000),
                new MenuDto("디너 오마카세", 150_000)
        );

        Restaurant restaurant = mock(Restaurant.class);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        menuService.add(restaurantId, menuDtoList);

        verify(menuRepository, times(2)).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 실패: 존재하지 않는 식당")
    void add_fail_no_restaurant() {
        Long restaurantId = 1L;

        List<MenuDto> menuDtoList = List.of(new MenuDto("런치 오마카세", 80_000));

        when(restaurantRepository.findById(restaurantId)).thenThrow(RestaurantNotFoundException.class);

        assertThatThrownBy(() -> menuService.add(restaurantId, menuDtoList))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    @DisplayName("메뉴 등록 실패: 중복된 메뉴명")
    void add_fail_duplicate_menu() {
        Long restaurantId = 1L;

        MenuDto menuDto = new MenuDto("런치 오마카세", 80_000);
        List<MenuDto> menuDtoList = List.of(menuDto);

        Restaurant restaurant = mock(Restaurant.class);

        Menu menu = new Menu(menuDto, restaurant);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuRepository.findByName("런치 오마카세")).thenReturn(Optional.of(menu));

        assertThatThrownBy(() -> menuService.add(restaurantId, menuDtoList))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("메뉴 전체 조회 성공")
    void viewAll() {
        Long restaurantId = 1L;

        Menu menu1 = mock(Menu.class);
        Menu menu2 = mock(Menu.class);

        List<Menu> menuList = List.of(menu1, menu2);

        when(menuRepository.findAllByRestaurantId(restaurantId)).thenReturn(menuList);

        List<MenuViewDto> menuViewDtoList = menuService.viewAll(restaurantId);
        assertThat(menuViewDtoList.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("메뉴 정보 업데이트 성공")
    void update_success() {
        Long menuId = 1L;
        MenuDto menuDto = new MenuDto("런치 오마카세", 80_000);
        MenuDto updateMenuDto = new MenuDto("디너 오마카세", 150_000);
        Restaurant restaurant = mock(Restaurant.class);
        Menu menu = new Menu(menuDto, restaurant);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        menuService.update(menuId, updateMenuDto);

        assertThat(menu.getName()).isEqualTo(updateMenuDto.getName());
        assertThat(menu.getPrice()).isEqualTo(updateMenuDto.getPrice());
    }

    @Test
    @DisplayName("메뉴 정보 업데이트 실패: 중복된 메뉴명")
    void update_fail() {
        Long menuId = 1L;
        MenuDto updateMenuDto = new MenuDto("디너 오마카세", 300_000);
        Menu menu = mock(Menu.class);

        when(menuRepository.findByName(updateMenuDto.getName())).thenReturn(Optional.of(menu));
        when(menu.getId()).thenReturn(2L);

        assertThatThrownBy(() -> menuService.update(menuId, updateMenuDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("메뉴 정보 삭제 성공")
    void delete() {
        Long menuId = 1L;

        menuService.delete(menuId);

        verify(menuRepository).deleteById(menuId);
    }

}