package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.web.SortByConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SortByConverter sortByConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(sortByConverter);
    }

}
