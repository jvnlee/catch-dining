package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.interceptor.DataSourceRouterCleanerInterceptor;
import com.jvnlee.catchdining.common.web.SortByConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SortByConverter sortByConverter;

    private final DataSourceRouterCleanerInterceptor dataSourceRouterCleanerInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(sortByConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataSourceRouterCleanerInterceptor);
    }

}
