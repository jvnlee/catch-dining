package com.jvnlee.catchdining.common.aspect;

import com.jvnlee.catchdining.common.annotation.ReadOnly;
import com.jvnlee.catchdining.common.util.DataSourceRouter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Aspect
@Component
@RequiredArgsConstructor
public class DataSourceRoutingAspect {

    private final DataSourceRouter dataSourceRouter;

    @Before("@annotation(readOnly)")
    public void routeToReadOnlyDataSource(ReadOnly readOnly) {
        dataSourceRouter.setReadOnly();
    }

}
