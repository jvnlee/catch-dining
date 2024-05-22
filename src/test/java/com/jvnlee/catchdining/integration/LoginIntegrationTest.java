package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import com.jvnlee.catchdining.domain.user.service.UserService;
import com.jvnlee.catchdining.TestContextInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.jvnlee.catchdining.domain.user.model.UserType.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(initializers = TestContextInitializer.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestContextInitializer.class)
class LoginIntegrationTest {

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
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        String responseAuthHeader = resultActions.andReturn().getResponse().getHeader(AUTHORIZATION);
        assertThat(responseAuthHeader).startsWith("Bearer");
        assertThat(responseAuthHeader.split(" ").length).isEqualTo(3);
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("username password 로그인 실패: 잘못된 username")
    void login_fail_username() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("wrong", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        resultActions
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("username password 로그인 실패: 잘못된 password")
    void login_fail_password() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "wrong");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        resultActions
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JWT 로그인 성공: Access(Valid) + Refresh(Valid)")
    void jwt_login_case_1() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions usernamePasswordLoginAction = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        String authHeader = usernamePasswordLoginAction.andReturn().getResponse().getHeader(AUTHORIZATION);

        ResultActions jwtLoginAction = mockMvc.perform(
                get("/someUrl")
                        .header(AUTHORIZATION, authHeader)
        );

        /*
         존재하지 않는 임의의 URL에 대한 GET 요청을 했을 때,
         403: 인증/인가가 안되었음
         404: 인증/인가는 되었으나 리소스가 존재하지 않음
         */
        jwtLoginAction.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("JWT 로그인 성공: Access(Valid) + Refresh(Invalid)")
    void jwt_login_case_2() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions usernamePasswordLoginAction = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        String authHeader = usernamePasswordLoginAction.andReturn().getResponse().getHeader(AUTHORIZATION);
        String[] split = authHeader.split(" ");
        String authHeaderWithInvalidRefreshToken = split[0] + " " + split[1] + " " + "InvalidRefreshToken";

        ResultActions jwtLoginAction = mockMvc.perform(
                get("/someUrl")
                        .header(AUTHORIZATION, authHeaderWithInvalidRefreshToken)
        );

        jwtLoginAction.andExpect(status().isNotFound()); // Refresh Token과 관계 없이 인증/인가 처리
    }

    @Test
    @DisplayName("JWT 로그인 성공: Access(Invalid) + Refresh(Valid)")
    void jwt_login_case_3() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions usernamePasswordLoginAction = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        String authHeader = usernamePasswordLoginAction.andReturn().getResponse().getHeader(AUTHORIZATION);

        Thread.sleep(5000); // 테스트 환경에서는 Access Token의 만료 기한을 3초로 잡아놓음 (만료시키기 위해 잠시 대기)

        ResultActions jwtLoginAction = mockMvc.perform(
                get("/someUrl")
                        .header(AUTHORIZATION, authHeader)
        );

        String newAuthHeader = jwtLoginAction.andReturn().getResponse().getHeader(AUTHORIZATION);
        assertThat(newAuthHeader).isNotEqualTo(authHeader); // Access Token이 재발급되었으므로 두 헤더가 같으면 안됨
        jwtLoginAction.andExpect(status().isNotFound()); // 재발급과 동시에 인증 처리시키므로 404 응답
    }

    @Test
    @DisplayName("JWT 로그인 실패: Access(Invalid) + Refresh(Invalid)")
    void jwt_login_case_4() throws Exception {
        UserDto userDto = new UserDto("user", "12345", "01012345678", CUSTOMER);
        userService.join(userDto);

        UserLoginDto userLoginDto = new UserLoginDto("user", "12345");
        String requestBody = om.writeValueAsString(userLoginDto);

        ResultActions usernamePasswordLoginAction = mockMvc.perform(
                post("/login")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .content(requestBody)
        );

        String authHeader = usernamePasswordLoginAction.andReturn().getResponse().getHeader(AUTHORIZATION);

        Thread.sleep(10000); // 두 토큰 모두 만료시키기 위해 10초 대기 (Access 3초, Refresh 6초)

        ResultActions jwtLoginAction = mockMvc.perform(
                get("/someUrl")
                        .header(AUTHORIZATION, authHeader)
        );

        jwtLoginAction.andExpect(status().isForbidden()); // 인증/인가 실패로 403 응답
    }

    @Test
    @DisplayName("JWT 로그인 실패: 잘못된 Authorization 헤더")
    void jwt_login_fail_invalid_header() throws Exception {
        String authHeader = "Invalid Auth Header";

        ResultActions jwtLoginAction = mockMvc.perform(
                get("/someUrl")
                        .header(AUTHORIZATION, authHeader)
        );

        jwtLoginAction.andExpect(status().isBadRequest()); // JwtExceptionFilter에 의해 400 응답
    }


}
