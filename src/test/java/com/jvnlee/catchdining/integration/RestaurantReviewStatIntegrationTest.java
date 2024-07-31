package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.util.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Transactional;

import static com.jvnlee.catchdining.domain.user.model.UserType.CUSTOMER;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@IntegrationTest
@Transactional
public class RestaurantReviewStatIntegrationTest extends TestcontainersContext {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper om;

    String authHeader;

    @BeforeEach
    void beforeEach() throws Exception {
        RestAssured.port = port;

        UserDto userJoinDto = new UserDto("andy", "12345", "01012345678", CUSTOMER);
        String userJoinRequestBody = om.writeValueAsString(userJoinDto);

        RestAssured
                .given().log().all()
                .body(userJoinRequestBody)
                .contentType(JSON)
                .when()
                .post("/users")
                .then().log().all();

        UserLoginDto userLoginDto = new UserLoginDto("andy", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .body(requestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all()
                .extract();

        authHeader = response.header(AUTHORIZATION);
    }

    @Test
    @DisplayName("READ-DB에 RestaurantReviewStat 생성 전파 테스트")
    void create_propagation_test() throws Exception {
        String restaurantName = "create_propagation_test";
        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name(restaurantName).build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        // WRITE-DB에 Restaurant 생성
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all()
                .extract();

        Long restaurantId = ((Integer) response.path("data.restaurantId")).longValue();

        Thread.sleep(1000);

        // READ-DB에서 조회하여 RestaurantReviewStat 생성 확인
        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .when()
                .get("/restaurants/{restaurantId}")
                .then().log().all()
                .assertThat()
                .body("data.name", equalTo(restaurantName));
    }

    @Test
    @DisplayName("READ-DB에 RestaurantReviewStat 별점, 리뷰 개수 업데이트 전파 테스트")
    void review_data_update_propagation_test() throws Exception {
        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name("review_data_update_propagation_test").build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        // WRITE-DB에 Restaurant 생성
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all()
                .extract();

        Long restaurantId = ((Integer) response.path("data.restaurantId")).longValue();

        ReviewCreateRequestDto reviewCreateDto = new ReviewCreateRequestDto(restaurantId, 4.0, 4.5, 5.0, "Love this place!");
        String reviewCreateRequestBody = om.writeValueAsString(reviewCreateDto);

        // WRITE-DB에 Review 생성
        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .body(reviewCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/reviews")
                .then().log().all();

        Thread.sleep(1000);

        // READ-DB에서 조회하여 RestaurantReviewStat 업데이트 반영 확인
        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .when()
                .get("/restaurants/{restaurantId}")
                .then().log().all()
                .assertThat()
                .body("data.avgRating", equalTo(4.5f))
                .body("data.reviewCount", equalTo(1));
    }

    @Test
    @DisplayName("READ-DB에 RestaurantReviewStat 업데이트 전파 테스트")
    void update_propagation_test() throws Exception {
        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name("update_propagation_test").build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        // WRITE-DB에 Restaurant 생성
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all()
                .extract();

        Long restaurantId = ((Integer) response.path("data.restaurantId")).longValue();

        String updatedRestaurantName = "update_propagation_test_2";
        RestaurantDto restaurantUpdateDto = RestaurantDto.builder().name(updatedRestaurantName).build();
        String restaurantUpdateRequestBody = om.writeValueAsString(restaurantUpdateDto);

        // WRITE-DB에 Restaurant 업데이트
        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .body(restaurantUpdateRequestBody)
                .contentType(JSON)
                .when()
                .put("/restaurants/{restaurantId}")
                .then().log().all();

        Thread.sleep(1000);

        // READ-DB에서 조회하여 RestaurantReviewStat 업데이트 반영 확인
        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .pathParam("restaurantId", restaurantId)
                .when()
                .get("/restaurants/{restaurantId}")
                .then().log().all()
                .assertThat()
                .body("data.name", equalTo(updatedRestaurantName));
    }

    @Test
    @DisplayName("READ-DB에 RestaurantReviewStat 삭제 전파 테스트")
    void delete_propagation_test() throws Exception {
        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name("delete_propagation_test").build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        // WRITE-DB에 Restaurant 생성
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all()
                .extract();

        Long restaurantId = ((Integer) response.path("data.restaurantId")).longValue();

        // WRITE-DB에 Restaurant 삭제
        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .when()
                .delete("/restaurants/{restaurantId}")
                .then().log().all();

        Thread.sleep(1000);

        // READ-DB에서 조회하여 RestaurantReviewStat 삭제 반영 확인
        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .pathParam("restaurantId", restaurantId)
                .when()
                .get("/restaurants/{restaurantId}")
                .then().log().all()
                .assertThat()
                .body("message", equalTo("식당 정보가 존재하지 않습니다."));
    }

}
