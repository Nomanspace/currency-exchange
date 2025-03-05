package org.nomanspace.currencyexchange.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String url;
    private final String username;
    private final String password;

    public DBConnection() {
        this.url = "jdbc:postgresql://localhost:5432/currencyexchange";
        this.username = "postgres";
        this.password = "Soulstorm1";
    }

    public void testConnection() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ Подключение к базе данных успешно!");
        } catch (SQLException e) {
            System.err.println("❌ Ошибка подключения: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful!");
            return connection;
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
