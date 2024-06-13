package com.jvnlee.catchdining.common.aspect;

import com.jvnlee.catchdining.common.annotation.AggregatedData;
import com.jvnlee.catchdining.common.util.DataSourceRouter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DataSourceRoutingAspect {

    private final DataSourceRouter dataSourceRouter;

    @Before("@annotation(aggregatedData)")
    public void routeToReadOnlyDataSource(AggregatedData aggregatedData) {
        dataSourceRouter.setReadOnly();
    }

}
