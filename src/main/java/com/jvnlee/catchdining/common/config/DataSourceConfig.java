package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.util.DataSourceRouter;
import com.jvnlee.catchdining.common.util.DataSourceType;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@Profile("!test")
public class DataSourceConfig {

    @Value("${spring.datasource.read-db.url}")
    private String READ_DB_URL;

    @Value("${spring.datasource.read-db.username}")
    private String READ_DB_USERNAME;

    @Value("${spring.datasource.read-db.password}")
    private String READ_DB_PASSWORD;

    @Value("${spring.datasource.url}")
    private String WRITE_DB_URL;

    @Value("${spring.datasource.username}")
    private String WRITE_DB_USERNAME;

    @Value("${spring.datasource.password}")
    private String WRITE_DB_PASSWORD;

    @Value("${spring.datasource.read-db.driver-class-name}")
    private String READ_DB_DRIVER_CLASS_NAME;

    @Value("${spring.datasource.driver-class-name}")
    private String WRITE_DB_DRIVER_CLASS_NAME;

    private DataSource writeDataSource() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(WRITE_DB_URL);
        dataSourceProperties.setUsername(WRITE_DB_USERNAME);
        dataSourceProperties.setPassword(WRITE_DB_PASSWORD);
        dataSourceProperties.setDriverClassName(WRITE_DB_DRIVER_CLASS_NAME);

        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    private DataSource readDataSource() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(READ_DB_URL);
        dataSourceProperties.setUsername(READ_DB_USERNAME);
        dataSourceProperties.setPassword(READ_DB_PASSWORD);
        dataSourceProperties.setDriverClassName(READ_DB_DRIVER_CLASS_NAME);

        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "dataSourceRouter")
    public DataSourceRouter dataSourceRouter() {
        HashMap<Object, Object> dataSourceMap = new HashMap<>();
        final DataSource writeDataSource = writeDataSource();
        dataSourceMap.put(DataSourceType.WRITE_ONLY, writeDataSource);
        dataSourceMap.put(DataSourceType.READ_ONLY, readDataSource());

        DataSourceRouter dataSourceRouter = new DataSourceRouter();
        dataSourceRouter.setTargetDataSources(dataSourceMap);
        dataSourceRouter.setDefaultTargetDataSource(writeDataSource);

        return dataSourceRouter;
    }

    @Bean(name = "lazyDataSource")
    public DataSource lazyDataSource(@Qualifier("dataSourceRouter") DataSource dataSource) {
        return new LazyConnectionDataSourceProxy(dataSource);
    }

}
