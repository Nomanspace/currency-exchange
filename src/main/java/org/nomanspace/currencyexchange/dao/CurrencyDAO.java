package org.nomanspace.currencyexchange.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CurrencyDAO {
    DBConnection dbConnection;

    public CurrencyDAO() {
        dbConnection = new DBConnection();
    }

    public void updateData(String code, String fullName, String sign) throws SQLException {
        //UPDATE table_name SET column1 = 'value' WHERE condition
        String sqlUpdate = "INSERT INTO currencies (code, fullName, sign) VALUES (?,?,?)";
        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate);
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, sign);
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
        }

    }
}
