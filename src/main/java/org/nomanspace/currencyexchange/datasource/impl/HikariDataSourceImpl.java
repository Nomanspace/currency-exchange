package org.nomanspace.currencyexchange.datasource.impl;

import org.nomanspace.currencyexchange.datasource.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.nomanspace.currencyexchange.datasource.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariDataSourceImpl implements DataSource {
    private HikariConfig config;
    private DatabaseConfig dConfig;
    private HikariDataSource hDSource;

    public HikariDataSourceImpl(DatabaseConfig dConfig) {
        config = new HikariConfig();
        this.dConfig = dConfig;
        hDSource = hikariInit();
    }

    /**
     *
     */
    @Override
    public void closeConnectionPool() {
        hDSource.close();
    }

    public HikariDataSource hikariInit() {
        config.setJdbcUrl(dConfig.getUrl());
        config.setUsername(dConfig.getUsername());
        config.setPassword(dConfig.getPassword());

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);

        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(60_000);
        config.setMaxLifetime(1_800_000);
        config.setKeepaliveTime(0);
        config.setValidationTimeout(5_000);
        config.setInitializationFailTimeout(1);

        config.setAutoCommit(true);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.setReadOnly(false);
        config.setCatalog(null);
        config.setSchema(null);

        config.setPoolName("currency-pool");
        config.setLeakDetectionThreshold(0);
        config.setRegisterMbeans(false);

        return new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hDSource.getConnection();
    }


}
