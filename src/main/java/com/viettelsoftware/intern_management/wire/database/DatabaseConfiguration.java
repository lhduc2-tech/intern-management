package com.viettelsoftware.intern_management.wire.database;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"com.viettelsoftware.intern_management"}, enableDefaultTransactions = false)
public class DatabaseConfiguration {

    private final DataSource dataSource;

    @Autowired
    public DatabaseConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logDataSourceDetails() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.info("HikariCP DataSource Details:");
            log.info("JDBC URL: {}", hikariDataSource.getJdbcUrl());
            log.info("Username: {}", hikariDataSource.getUsername());
            log.info("Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
            log.info("Minimum Idle Connections: {}", hikariDataSource.getMinimumIdle());
        } else {
            log.info("DataSource is not an instance of HikariDataSource");
        }
    }
}
