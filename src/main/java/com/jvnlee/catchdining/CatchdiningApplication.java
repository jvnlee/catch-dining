package com.jvnlee.catchdining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CatchdiningApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatchdiningApplication.class, args);
	}

}
