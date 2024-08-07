package com.jvnlee.catchdining.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

public class TestcontainersContext {

    private static final String MYSQL_IMAGE = "mysql:8.0.32";
    private static final JdbcDatabaseContainer<?> WRITE_DB;
    private static final JdbcDatabaseContainer<?> READ_DB;
    private static final String DATABASE_NAME = "catch_dining";
    private static final String WRITE_DB_INIT_SCRIPT_NAME = "init-write-db.test.sql";
    private static final String READ_DB_INIT_SCRIPT_NAME = "init-read-db.test.sql";

    private static final String REDIS_IMAGE = "redis:7.0.14";
    private static final GenericContainer<?> REDIS;
    private static final int REDIS_PORT = 6379;

    private static final String RABBITMQ_IMAGE = "rabbitmq:3.13.6";
    private static final RabbitMQContainer RABBITMQ;
    private static final int RABBITMQ_PORT = 5672;

    static {
        WRITE_DB = new MySQLContainer<>(MYSQL_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withInitScript(WRITE_DB_INIT_SCRIPT_NAME);
        READ_DB = new MySQLContainer<>(MYSQL_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withInitScript(READ_DB_INIT_SCRIPT_NAME);
        REDIS = new GenericContainer<>(REDIS_IMAGE)
                .withExposedPorts(REDIS_PORT);
        RABBITMQ = new RabbitMQContainer(RABBITMQ_IMAGE)
                .withExposedPorts(RABBITMQ_PORT);

        WRITE_DB.start();
        READ_DB.start();
        REDIS.start();
        RABBITMQ.start();
    }

    @DynamicPropertySource
    protected static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", WRITE_DB::getJdbcUrl);
        registry.add("spring.datasource.username", WRITE_DB::getUsername);
        registry.add("spring.datasource.password", WRITE_DB::getPassword);
        registry.add("spring.datasource.driver-class-name", WRITE_DB::getDriverClassName);

        registry.add("spring.datasource.read-db.url", READ_DB::getJdbcUrl);
        registry.add("spring.datasource.read-db.username", READ_DB::getUsername);
        registry.add("spring.datasource.read-db.password", READ_DB::getPassword);
        registry.add("spring.datasource.read-db.driver-class-name", READ_DB::getDriverClassName);

        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(REDIS_PORT).toString());

        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", () -> RABBITMQ.getMappedPort(RABBITMQ_PORT).toString());
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

}
