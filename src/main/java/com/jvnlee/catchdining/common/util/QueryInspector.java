package com.jvnlee.catchdining.common.util;

import lombok.Getter;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Profile("dev")
@Component
public class QueryInspector implements StatementInspector {

    private final Long requestStartTime = System.currentTimeMillis();

    private ThreadLocal<Integer> executionCount = ThreadLocal.withInitial(() -> 0);

    public Long getElapsedTime() {
        return System.currentTimeMillis() - requestStartTime;
    }

    @Override
    public String inspect(String sql) {
        incrementExecutionCount();
        return sql;
    }

    private void incrementExecutionCount() {
        Integer prevCount = executionCount.get();
        executionCount.set(prevCount + 1);
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void resetExecutionCount() {
        executionCount.set(0);
    }

}
