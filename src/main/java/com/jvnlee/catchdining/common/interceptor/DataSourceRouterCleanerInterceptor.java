package com.jvnlee.catchdining.common.interceptor;

import com.jvnlee.catchdining.common.util.DataSourceRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class DataSourceRouterCleanerInterceptor implements HandlerInterceptor {

    private final DataSourceRouter dataSourceRouter;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        dataSourceRouter.reset();
    }

}
