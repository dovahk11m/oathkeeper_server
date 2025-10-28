package com.oath.common.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        // 1. h2 리포지토리 위치만 지정
        basePackages = "com.oath.domain",
        // 제외할 패키지(정규식)
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                // com.oath.domain.test 의 하위 패키지들을 h2에서 제외
                pattern = "com.oath.domain\\.recommend\\..*"
        ),
        // 2. 이 클래스에서 생성할 Factory와 Manager를 명시
        entityManagerFactoryRef = "h2EntityManagerFactory",
        transactionManagerRef = "h2TransactionManager"
)
public class H2JpaConfig {

    @Primary
    @Bean(name = "h2JpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.h2-props")
    public JpaProperties h2JpaProperties() {
        return new JpaProperties();
    }

    @Primary
    @Bean(name = "h2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean h2EntityManagerFactory(
            @Qualifier("h2DataSource") DataSource h2DataSource,
            @Qualifier("h2JpaProperties") JpaProperties h2JpaProperties,
            EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(h2DataSource)
                .packages("com.oath.domain")
                .properties(h2JpaProperties.getProperties())
                .build();
    }

    @Primary
    @Bean(name = "h2TransactionManager")
    public PlatformTransactionManager h2TransactionManager(
            @Qualifier("h2EntityManagerFactory") EntityManagerFactory h2EntityManagerFactory) {
        return new JpaTransactionManager(h2EntityManagerFactory);
    }
}
