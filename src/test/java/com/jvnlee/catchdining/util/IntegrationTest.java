package com.jvnlee.catchdining.util;

import com.jvnlee.catchdining.TestContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestContextInitializer.class)
public @interface IntegrationTest {
}
