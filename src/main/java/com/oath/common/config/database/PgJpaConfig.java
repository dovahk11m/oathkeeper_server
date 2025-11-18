package com.oath.common.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        // PostgreSQL은 recommend_domain의 Repository만 담당하도록 역할을 명확히 함
        basePackages = "com.oath.recommend_domain",
        entityManagerFactoryRef = "pgEntityManagerFactory",
        transactionManagerRef = "pgTransactionManager"
)
public class PgJpaConfig {

    @Bean(name = "pgJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.pg-props")
    public JpaProperties pgJpaProperties() {
        return new JpaProperties();
    }

    @Bean(name = "pgEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pgEntityManagerFactory(
            @Qualifier("pgDataSource") DataSource pgDataSource,
            @Qualifier("pgJpaProperties") JpaProperties pgJpaProperties,
            EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(pgDataSource)
                // 엔티티 패키지도 recommend_domain만 담당하도록 수정
                .packages("com.oath.recommend_domain")
                .properties(pgJpaProperties.getProperties())
                .build();
    }

    @Bean(name = "pgTransactionManager")
    public PlatformTransactionManager pgTransactionManager(
            @Qualifier("pgEntityManagerFactory") EntityManagerFactory pgEntityManagerFactory) {
        return new JpaTransactionManager(pgEntityManagerFactory);
    }
}
