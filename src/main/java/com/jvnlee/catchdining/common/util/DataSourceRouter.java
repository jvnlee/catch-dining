package com.jvnlee.catchdining.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DataSourceRouter extends AbstractRoutingDataSource {

    private final ThreadLocal<DataSourceType> currentDataSource = ThreadLocal.withInitial(() -> DataSourceType.WRITE_ONLY);

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = currentDataSource.get();

        log.info("Current Data Source: {}", dataSourceType.name());

        return dataSourceType;
    }

}
