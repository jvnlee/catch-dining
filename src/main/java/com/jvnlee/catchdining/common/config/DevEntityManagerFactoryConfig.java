package com.jvnlee.catchdining.common.config;

import com.jvnlee.catchdining.common.util.QueryInspector;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevEntityManagerFactoryConfig {

    private final QueryInspector queryInspector;

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean devEntityManagerFactory(
            @Qualifier("lazyDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.jvnlee.catchdining");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(Map.of(
                AvailableSettings.STATEMENT_INSPECTOR, queryInspector,
                AvailableSettings.FORMAT_SQL, true
        ));
        return em;
    }

}
