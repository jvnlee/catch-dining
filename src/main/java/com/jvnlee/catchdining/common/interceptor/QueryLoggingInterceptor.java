package com.jvnlee.catchdining.common.interceptor;

import com.jvnlee.catchdining.common.util.QueryInspector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryLoggingInterceptor implements HandlerInterceptor {

    private final QueryInspector queryInspector;

    private static final String QUERY_LOG_INFO_FORMAT = "\n" +
            "- HTTP Method: {}\n" +
            "- URI: {}\n" +
            "- Elapsed Time: {}ms\n" +
            "- Query Execution Count: {}";

    private static final String QUERY_LOG_WARN_FORMAT = "\n" +
            "쿼리가 실행 횟수가 기준치 {}회를 초과했습니다.\n" +
            "- Query Execution Count: {}";

    private static final int QUERY_LOG_WARN_THRESHOLD = 10;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable Exception ex) throws Exception {
        Long elapsedTime = queryInspector.getElapsedTime();
        int executionCount = queryInspector.getExecutionCount();

        log.info(
            QUERY_LOG_INFO_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            elapsedTime,
            executionCount
        );

        if (executionCount >= QUERY_LOG_WARN_THRESHOLD) {
            log.warn(
                QUERY_LOG_WARN_FORMAT,
                QUERY_LOG_WARN_THRESHOLD,
                executionCount
            );
        }
    }
}
