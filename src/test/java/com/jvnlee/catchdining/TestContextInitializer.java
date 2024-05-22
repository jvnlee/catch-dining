package com.jvnlee.catchdining;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class TestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        RedisContainerManager.start();
    }

    static class RedisContainerManager {
        private static GenericContainer redis;

        private RedisContainerManager() {}

        public static void start() {
            if (redis == null) {
                redis = new GenericContainer("redis:7.0.14")
                        .withExposedPorts(6379);

                redis.start();

                System.setProperty("spring.redis.host", redis.getHost());
                System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());
            }
        }
    }

}
