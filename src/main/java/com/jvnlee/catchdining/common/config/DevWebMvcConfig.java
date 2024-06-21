package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.interceptor.QueryLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile({"dev", "test"})
@Configuration
@RequiredArgsConstructor
public class DevWebMvcConfig implements WebMvcConfigurer {

    private final QueryLoggingInterceptor queryLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queryLoggingInterceptor);
    }

}
