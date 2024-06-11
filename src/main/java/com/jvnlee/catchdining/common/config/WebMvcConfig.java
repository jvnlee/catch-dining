package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.interceptor.DataSourceRouterCleanerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DataSourceRouterCleanerInterceptor dataSourceRouterCleanerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataSourceRouterCleanerInterceptor);
    }

}
