package com.jvnlee.catchdining.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class DataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? DataSourceType.READ_ONLY
                : DataSourceType.WRITE_ONLY;

        log.info("Current Data Source: {}", dataSourceType.name());

        return dataSourceType;
    }

}
