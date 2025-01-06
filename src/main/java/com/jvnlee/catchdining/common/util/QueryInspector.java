package com.jvnlee.catchdining.common.util;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"dev", "test"})
@Component
public class QueryInspector implements StatementInspector {

    private ThreadLocal<Long> requestStartTime = new ThreadLocal<>();

    private ThreadLocal<Integer> executionCount = ThreadLocal.withInitial(() -> 0);

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

    public void initializeExecutionCount() {
        executionCount.set(0);
    }

    private void incrementExecutionCount() {
        executionCount.set(executionCount.get() + 1);
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void resetExecutionCount() {
        executionCount.remove();
    }

}
