package com.oath.common.config.database;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // 1. H2 DB 설정
    @Primary
    @Bean(name = "h2DataSourceCustom")
    public DataSource h2DataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:oath");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        // MySQL 모드 + 테이블/컬럼 이름 그대로
        ds.addDataSourceProperty("MODE", "MySQL");
        ds.addDataSourceProperty("DATABASE_TO_UPPER", "false");

        return ds;
    }
    // 2. MySQL 데이터소스 설정
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 3. PostgreSQL 데이터소스 설정
    @Bean(name = "pgDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.pg")
    public DataSource pgDataSource() {
        return DataSourceBuilder.create().build();
    }
}
