package com.jvnlee.catchdining;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestContextInitializer.class)
class CatchdiningApplicationTests {

	@Test
	void contextLoads() {
	}

}
