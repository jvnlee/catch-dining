package com.jvnlee.catchdining.domain.menu.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.dto.MenuViewDto;
import com.jvnlee.catchdining.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> add(@PathVariable Long restaurantId, @RequestBody List<MenuDto> menuDtoList) {
        menuService.add(restaurantId, menuDtoList);
        return new Response<>("메뉴 등록 성공");
    }

    @GetMapping
    public Response<List<MenuViewDto>> viewAll(@PathVariable Long restaurantId) {
        List<MenuViewDto> data = menuService.viewAll(restaurantId);
        return new Response<>("메뉴 조회 결과", data);
    }

    @PutMapping("/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> update(@PathVariable Long menuId, @RequestBody MenuDto menuDto) {
        menuService.update(menuId, menuDto);
        return new Response<>("메뉴 정보 업데이트 성공");
    }

    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> delete(@PathVariable Long menuId) {
        menuService.delete(menuId);
        return new Response<>("메뉴 삭제 성공");
    }

}
