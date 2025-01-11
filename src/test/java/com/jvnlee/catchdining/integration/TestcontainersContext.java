package com.jvnlee.catchdining.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.List;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class TestcontainersContext {

    private static final String WRITE_DB = "write-db";
    private static final String READ_DB = "read-db";
    private static final int MYSQL_PORT = 3306;
    private static final String DATABASE_NAME = "catch_dining";
    private static final String JDBC_URL_FORMAT = "jdbc:mysql://%s:%d/%s";

    private static final String REDIS = "redis";
    private static final int REDIS_PORT = 6379;

    private static final String RABBITMQ = "rabbitmq";
    private static final int RABBITMQ_PORT = 5672;

    private static final ComposeContainer COMPOSE_CONTAINER;
    private static final String COMPOSE_FILE_PATH = "src/test/resources/docker-compose.test.yml";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    static {
        COMPOSE_CONTAINER =
                new ComposeContainer(new File(COMPOSE_FILE_PATH))
                        .withExposedService(WRITE_DB, MYSQL_PORT, Wait.forHealthcheck())
                        .withExposedService(READ_DB, MYSQL_PORT, Wait.forHealthcheck())
                        .withExposedService(REDIS, REDIS_PORT)
                        .withExposedService(RABBITMQ, RABBITMQ_PORT);

        COMPOSE_CONTAINER.start();
    }

    @DynamicPropertySource
    protected static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                String.format(
                        JDBC_URL_FORMAT,
                        COMPOSE_CONTAINER.getServiceHost(WRITE_DB, MYSQL_PORT),
                        COMPOSE_CONTAINER.getServicePort(WRITE_DB, MYSQL_PORT),
                        DATABASE_NAME
                )
        );

        registry.add("spring.datasource.read-db.url", () ->
                String.format(
                        JDBC_URL_FORMAT,
                        COMPOSE_CONTAINER.getServiceHost(READ_DB, MYSQL_PORT),
                        COMPOSE_CONTAINER.getServicePort(READ_DB, MYSQL_PORT),
                        DATABASE_NAME
                )
        );

        registry.add("spring.redis.host", () -> COMPOSE_CONTAINER.getServiceHost(REDIS, REDIS_PORT));
        registry.add("spring.redis.port", () -> COMPOSE_CONTAINER.getServicePort(REDIS, REDIS_PORT));

        registry.add("spring.rabbitmq.host", () -> COMPOSE_CONTAINER.getServiceHost(RABBITMQ, RABBITMQ_PORT));
        registry.add("spring.rabbitmq.port", () -> COMPOSE_CONTAINER.getServicePort(RABBITMQ, RABBITMQ_PORT));
    }

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @AfterAll
    void cleanup() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = ?",
                String.class,
                DATABASE_NAME
        );

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`");
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        redisTemplate.execute((RedisConnection connection) -> {
            connection.flushAll();
            return null;
        });
    }

}
