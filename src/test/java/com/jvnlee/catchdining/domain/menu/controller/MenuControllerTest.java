package com.jvnlee.catchdining.domain.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.dto.MenuViewDto;
import com.jvnlee.catchdining.domain.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("controller-test")
@WebMvcTest(
        controllers = {MenuController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@WithMockUser(username = "user", roles = {"OWNER"})
@MockBean(JpaMetamodelMappingContext.class)
class MenuControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    MenuService menuService;

    @Test
    @DisplayName("메뉴 등록 성공")
    void add_success() throws Exception {
        Long restaurantId = 1L;

        List<MenuDto> menuDtoList = List.of(
                new MenuDto("런치 오마카세", 80_000),
                new MenuDto("디너 오마카세", 150_000)
        );

        String requestBody = om.writeValueAsString(menuDtoList);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/menus", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(menuService).add(restaurantId, menuDtoList);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메뉴 등록 성공"));
    }

    @Test
    @DisplayName("메뉴 등록 실패: 중복된 메뉴명")
    void add_fail() throws Exception {
        Long restaurantId = 1L;

        List<MenuDto> menuDtoList = List.of(
                new MenuDto("런치 오마카세", 80_000),
                new MenuDto("디너 오마카세", 150_000)
        );

        String requestBody = om.writeValueAsString(menuDtoList);

        doThrow(new DuplicateKeyException("이미 존재하는 메뉴명입니다.")).when(menuService).add(restaurantId, menuDtoList);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/menus", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(menuService).add(restaurantId, menuDtoList);
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 메뉴명입니다."));
    }

    @Test
    @DisplayName("메뉴 전체 조회 성공")
    void viewAll() throws Exception {
        Long restaurantId = 1L;

        List<MenuViewDto> menuViewDtoList = List.of(
                new MenuViewDto(1L, "런치 오마카세", 80_000)
        );

        when(menuService.viewAll(restaurantId)).thenReturn(menuViewDtoList);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}/menus", restaurantId)
        );

        verify(menuService).viewAll(restaurantId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메뉴 조회 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("메뉴 정보 업데이트 성공")
    void update_success() throws Exception {
        Long restaurantId = 1L;
        Long menuId = 1L;

        MenuDto updateMenuDto = new MenuDto("런치 오마카세", 80_000);

        String requestBody = om.writeValueAsString(updateMenuDto);

        ResultActions resultActions = mockMvc.perform(
                put("/restaurants/{restaurantId}/menus/{menuId}", restaurantId, menuId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(menuService).update(menuId, updateMenuDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메뉴 정보 업데이트 성공"));
    }

    @Test
    @DisplayName("메뉴 정보 업데이트 실패")
    void update_fail() throws Exception {
        Long restaurantId = 1L;
        Long menuId = 1L;

        MenuDto updateMenuDto = new MenuDto("런치 오마카세", 80_000);

        String requestBody = om.writeValueAsString(updateMenuDto);

        doThrow(new DuplicateKeyException("이미 존재하는 메뉴명입니다.")).when(menuService).update(menuId, updateMenuDto);

        ResultActions resultActions = mockMvc.perform(
                put("/restaurants/{restaurantId}/menus/{menuId}", restaurantId, menuId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(menuService).update(menuId, updateMenuDto);
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 메뉴명입니다."));
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void delete_success() throws Exception {
        Long restaurantId = 1L;
        Long menuId = 1L;

        ResultActions resultActions = mockMvc.perform(
                delete("/restaurants/{restaurantId}/menus/{menuId}", restaurantId, menuId)
        );

        verify(menuService).delete(menuId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메뉴 삭제 성공"));
    }

}