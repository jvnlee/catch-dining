package com.jvnlee.catchdining.common.util;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"dev", "test"})
@Component
public class QueryInspector implements StatementInspector {

    private ThreadLocal<Long> requestStartTime = new ThreadLocal<>();

    private ThreadLocal<Integer> executionCount = new ThreadLocal<>();

    @Override
    public String inspect(String sql) {
        incrementExecutionCount();
        return sql;
    }

    public void initializeRequestStartTime() {
        requestStartTime.set(System.currentTimeMillis());
    }

    public Long getElapsedTime() {
        return System.currentTimeMillis() - requestStartTime.get();
    }

    public void resetRequestStartTime() {
        requestStartTime.remove();
    }

    private void incrementExecutionCount() {
        Integer prevCount = executionCount.get();
        executionCount.set(prevCount + 1);
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void resetExecutionCount() {
        executionCount.remove();
    }

}
