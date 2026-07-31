package org.nomanspace.currencyexchange.datasource;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SavageDataSourceImpl implements DataSource {
    DatabaseConfig databaseConfig;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found!!", e);
        }
    }

    public SavageDataSourceImpl(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig ;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(databaseConfig.getUrl(),
                    databaseConfig.getUsername(), databaseConfig.getPassword());
            System.out.println("Connection successful!");
            return connection;
        } catch (SQLException e) {
            System.err.println("ЖОПА Connection failed: " + e.getMessage());
            throw e;
        }
    }
}
