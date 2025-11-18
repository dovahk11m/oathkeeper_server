package com.oath.common.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        // 1. h2 리포지토리 위치만 지정
        basePackages = "com.oath.domain",
        // 2. 이 클래스에서 생성할 Factory와 Manager를 명시
        entityManagerFactoryRef = "h2EntityManagerFactory",
        transactionManagerRef = "h2TransactionManager"
)
public class H2JpaConfig {

    @Primary // Spring Boot 자동 설정이 JpaProperties Bean을 하나만 찾도록 하기 위해 Primary로 지정
    @Bean(name = "h2JpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.h2-props")
    public JpaProperties h2JpaProperties() {
        return new JpaProperties();
    }

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

    @Primary // 여러 TransactionManager 중, 명시적 지정이 없을 때를 대비한 기본값으로 사용
    @Bean(name = "h2TransactionManager")
    public PlatformTransactionManager h2TransactionManager(
            @Qualifier("h2EntityManagerFactory") EntityManagerFactory h2EntityManagerFactory) {
        return new JpaTransactionManager(h2EntityManagerFactory);
    }
}
