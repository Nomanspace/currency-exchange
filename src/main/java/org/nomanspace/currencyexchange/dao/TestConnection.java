package org.nomanspace.currencyexchange.dao;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) throws SQLException {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        //currencyDAO.dbConnection.testConnection();
        currencyDAO.updateData("GBP", "British Pound Sterling", "€");
    }
}
