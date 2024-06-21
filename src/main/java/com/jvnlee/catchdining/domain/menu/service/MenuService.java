package com.jvnlee.catchdining.domain.menu.service;

import com.jvnlee.catchdining.common.exception.MenuNotFoundException;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.menu.model.Menu;
import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.dto.MenuViewDto;
import com.jvnlee.catchdining.domain.menu.repository.MenuRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    private final RestaurantRepository restaurantRepository;

    public void add(Long restaurantId, List<MenuDto> menuDtoList) {
        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);
        for (MenuDto menuDto : menuDtoList) {
            validateName(menuDto.getName());
            Menu menu = new Menu(menuDto, restaurant);
            menuRepository.save(menu);
        }
    }

    @Transactional(readOnly = true)
    public List<MenuViewDto> viewAll(Long restaurantId) {
        List<Menu> menuList = menuRepository.findAllByRestaurantId(restaurantId);
        return menuList
                .stream()
                .map(MenuViewDto::new)
                .collect(toList());
    }

    public void update(Long menuId, MenuDto menuDto) {
        validateName(menuId, menuDto.getName());
        Menu menu = menuRepository
                .findById(menuId)
                .orElseThrow(MenuNotFoundException::new);
        menu.update(menuDto);
    }

    public void delete(Long menuId) {
        menuRepository.deleteById(menuId);
    }

    private void validateName(String name) {
        if (menuRepository.findByName(name).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 메뉴명입니다.");
        }
    }

    private void validateName(Long menuId, String name) {
        Optional<Menu> menu = menuRepository.findByName(name);
        if (menu.isPresent()) {
            if (menu.get().getId().equals(menuId)) return;
            throw new DuplicateKeyException("이미 존재하는 메뉴명입니다.");
        }
    }

}
