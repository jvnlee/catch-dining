package com.jvnlee.catchdining.common.web;

import com.jvnlee.catchdining.domain.restaurant.model.SortBy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortByConverter implements Converter<String, SortBy> {

    @Override
    public SortBy convert(String source) {
        try {
            return SortBy.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 정렬 파라미터입니다.");
        }
    }

}
