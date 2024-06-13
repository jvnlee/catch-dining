package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import com.jvnlee.catchdining.domain.user.service.UserService;
import com.jvnlee.catchdining.util.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.jvnlee.catchdining.domain.user.model.UserType.*;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;

@IntegrationTest
class LoginIntegrationTest extends TestcontainersContext {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper om;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;

        UserDto userDto = new UserDto("andy", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("username password 로그인 성공")
    void login_success() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto("andy", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        RestAssured
                .given().log().all()
                .body(requestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all()
                .assertThat()
                .statusCode(OK.value())
                .header(AUTHORIZATION, startsWith("Bearer"))
                .header(AUTHORIZATION, (header) -> header.split(" ").length, equalTo(3));
    }

    @Test
    @DisplayName("username password 로그인 실패: 잘못된 username")
    void login_fail_username() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto("wrong", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        RestAssured
                .given().log().all()
                .body(requestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("message", equalTo("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("username password 로그인 실패: 잘못된 password")
    void login_fail_password() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto("andy", "wrong");
        String requestBody = om.writeValueAsString(userLoginDto);

        RestAssured
                .given().log().all()
                .body(requestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("message", equalTo("비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("JWT 로그인 성공: Access(Valid) + Refresh(Valid)")
    void jwt_login_case_1() throws Exception {
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

        /*
         임의의 URL에 대한 GET 요청을 했을 때,
         401: 인증이 안되었음
         404: 인증은 되었으나 리소스가 존재하지 않음
         */
        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, response.header(AUTHORIZATION))
                .when()
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    @DisplayName("JWT 로그인 성공: Access(Valid) + Refresh(Invalid)")
    void jwt_login_case_2() throws Exception {
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

        String validAccessToken = response.header(AUTHORIZATION).split(" ")[1];

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, "Bearer " + validAccessToken + " " + "InvalidRefreshToken")
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(NOT_FOUND.value()); // Refresh Token과 관계 없이 인증 처리
    }

    @Test
    @DisplayName("JWT 로그인 실패: Access(Invalid) + Refresh(Valid)")
    void jwt_login_case_3() throws Exception {
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

        String authHeader = response.header(AUTHORIZATION);
        String validRefreshToken = authHeader.split(" ")[2];

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, "Bearer InvalidRefreshToken " + validRefreshToken)
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    @DisplayName("JWT 로그인 실패: Access(Invalid) + Refresh(Invalid)")
    void jwt_login_case_4() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto("andy", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        RestAssured
                .given().log().all()
                .body(requestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all();

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, "Bearer InvalidAccessToken InvalidRefreshToken")
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("JWT 로그인 실패: 3단 구조가 아닌 유효하지 않은 Authorization 헤더")
    void jwt_login_fail_invalid_header_structure() {
        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, "Invalid Auth Header 123123")
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(BAD_REQUEST.value()) // JwtExceptionFilter에 의해 400 응답
                .body("message", equalTo("Authorization header의 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("JWT 로그인 실패: Scheme이 Bearer가 아닌 유효하지 않은 Authorization 헤더")
    void jwt_login_fail_invalid_scheme() {
        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, "WrongScheme ... ...")
                .get("/someUrl")
                .then().log().all()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("message", equalTo("Authorization header의 scheme이 올바르지 않습니다."));
    }

}
