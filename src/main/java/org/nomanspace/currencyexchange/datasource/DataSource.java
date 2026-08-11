package org.nomanspace.currencyexchange.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
    public Connection getConnection() throws SQLException;

    public void closeConnectionPool();
}
