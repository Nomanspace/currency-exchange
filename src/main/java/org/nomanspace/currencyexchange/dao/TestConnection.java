package org.nomanspace.currencyexchange.dao;

import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) throws SQLException {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        //currencyDAO.dbConnection.testConnection();
        currencyDAO.updateData("USD", "United States dollar", "$");
    }
}
