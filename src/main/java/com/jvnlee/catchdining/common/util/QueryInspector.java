package com.jvnlee.catchdining.common.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@Profile("dev")
@Component
@RequestScope
@Getter
public class QueryInspector implements StatementInspector {

    private final Long requestStartTime = System.currentTimeMillis();

    private int executionCount;

    public Long getElapsedTime() {
        return System.currentTimeMillis() - requestStartTime;
    }

    @Override
    public String inspect(String sql) {
        executionCount++;
        return sql;
    }

}
