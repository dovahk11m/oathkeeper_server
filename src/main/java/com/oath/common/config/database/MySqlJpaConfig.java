//package com.oath.common.config.database;
//
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableJpaRepositories(
//        // 1. h2 리포지토리 위치만 지정
//        basePackages = "com.oath.mysql.repository",
//        // 2. 이 클래스에서 생성할 Factory와 Manager를 명시
//        entityManagerFactoryRef = "h2EntityManagerFactory",
//        transactionManagerRef = "h2TransactionManager"
//)
//public class MySqlJpaConfig {
//
//    @Bean(name = "mysqlJpaProperties")
//    @ConfigurationProperties(prefix = "spring.jpa.mysql-props")
//    public JpaProperties mysqlJpaProperties() {
//        return new JpaProperties();
//    }
//
//    @Bean(name = "mysqlEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
//            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
//            @Qualifier("mysqlJpaProperties") JpaProperties mysqlJpaProperties,
//            EntityManagerFactoryBuilder builder) {
//
//        return builder
//                .dataSource(mysqlDataSource)
//                .packages("com.oath.mysql.repository")
//                .properties(mysqlJpaProperties.getProperties())
//                .build();
//    }
//
//    @Bean(name = "mysqlTransactionManager")
//    public PlatformTransactionManager mysqlTransactionManager(
//            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory mysqlEntityManagerFactory) {
//        return new JpaTransactionManager(mysqlEntityManagerFactory);
//    }
//}
