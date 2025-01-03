package com.jvnlee.catchdining.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DataSourceRouter extends AbstractRoutingDataSource {

    private final ThreadLocal<DataSourceType> currentDataSource = ThreadLocal.withInitial(() -> DataSourceType.WRITE_DB);

    public void setReadOnly() {
        currentDataSource.set(DataSourceType.READ_DB);
    }

    public void reset() {
        currentDataSource.set(DataSourceType.WRITE_DB);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = currentDataSource.get();

        log.info("Current Data Source: {}", dataSourceType.name());

        return dataSourceType;
    }

}
