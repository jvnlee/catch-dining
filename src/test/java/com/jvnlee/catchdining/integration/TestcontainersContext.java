package com.jvnlee.catchdining.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public class TestcontainersContext {

    private static final String WRITE_DB = "write-db";
    private static final String READ_DB = "read-db";
    private static final int MYSQL_PORT = 3306;
    private static final String JDBC_URL_FORMAT = "jdbc:mysql://%s:%d/catch_dining";

    private static final String REDIS = "redis";
    private static final int REDIS_PORT = 6379;

    private static final String RABBITMQ = "rabbitmq";
    private static final int RABBITMQ_PORT = 5672;

    private static final ComposeContainer COMPOSE_CONTAINER;
    private static final String COMPOSE_FILE_PATH = "src/test/resources/docker-compose.test.yml";

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
    protected static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                String.format(
                        JDBC_URL_FORMAT,
                        COMPOSE_CONTAINER.getServiceHost(WRITE_DB, MYSQL_PORT),
                        COMPOSE_CONTAINER.getServicePort(WRITE_DB, MYSQL_PORT)
                )
        );

        registry.add("spring.datasource.read-db.url", () ->
                String.format(
                        JDBC_URL_FORMAT,
                        COMPOSE_CONTAINER.getServiceHost(READ_DB, MYSQL_PORT),
                        COMPOSE_CONTAINER.getServicePort(READ_DB, MYSQL_PORT)
                )
        );

        registry.add("spring.redis.host", () -> COMPOSE_CONTAINER.getServiceHost(REDIS, REDIS_PORT));
        registry.add("spring.redis.port", () -> COMPOSE_CONTAINER.getServicePort(REDIS, REDIS_PORT));

        registry.add("spring.rabbitmq.host", () -> COMPOSE_CONTAINER.getServiceHost(RABBITMQ, RABBITMQ_PORT));
        registry.add("spring.rabbitmq.port", () -> COMPOSE_CONTAINER.getServicePort(RABBITMQ, RABBITMQ_PORT));
    }

}
