package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.util.QueryInspector;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.hibernate.cfg.AvailableSettings.STATEMENT_INSPECTOR;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    private final QueryInspector queryInspector;

    @Bean
    public HibernatePropertiesCustomizer configureStatementInspector() {
        return hibernateProperties -> hibernateProperties.put(STATEMENT_INSPECTOR, queryInspector);
    }

}
