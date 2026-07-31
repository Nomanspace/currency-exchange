package org.nomanspace.currencyexchange.repository.impl;

import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityAlreadyExistsException;
import org.nomanspace.currencyexchange.model.Currency;


import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.datasource.DataSource;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CurrencyRepositoryImpl implements CurrencyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRepositoryImpl.class);
    private DataSource dataSource;

    public CurrencyRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Currency> save(Currency currency) {
        Optional<Currency> result = Optional.empty();
        String sqlUpdate = "INSERT INTO currencies (code, fullName, sign) VALUES (?,?,?) RETURNING id, code, fullName, sign";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)
        ) {
            //connection.setAutoCommit(false);
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, currency.getSign());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String code = resultSet.getString("code");
                    String fullName = resultSet.getString("fullName");
                    String sign = resultSet.getString("sign");
                    result = Optional.of(new Currency(id, code, fullName, sign));
                    LOGGER.info("The currency has been successfully added with parameter id {}, code {}, fullName {}, sign {}", id, code, fullName, sign);
                } else {
                    LOGGER.info("currency with code {} already existed and wasn't added or updated if you used do update ", currency.getCode());
                }
            }
        } catch (SQLException e) {
            if (PSQLState.UNIQUE_VIOLATION.getState().equals(e.getSQLState())) {
                throw new EntityAlreadyExistsException("Currency with code " + currency.getCode() + " already exists");
            }
            LOGGER.error("Error during save of record: {} ", currency, e);
            throw new DatabaseException("Database failed on save method : " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean delete(Integer id) {
        String deleteQuery = "DELETE FROM currencies WHERE id = ?";
        int rowsAffected = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)
        ) {
            preparedStatement.setInt(1, id);
            rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Successfully deleted record with ID: {}", id);
            } else {
                LOGGER.warn("Attempted to delete record with ID: {}, but it was not found.", id);
            }

        } catch (SQLException e) {
            LOGGER.error("Error during deletion of record with ID: {} ", id, e);
            throw new DatabaseException("Failed to delete currencies: " + e.getMessage(), e);
        }
        return rowsAffected > 0;
    }

    /**
     * @param entity
     * @return
     * @throws SQLException
     */

    @Override
    public Optional<Currency> update(Currency entity) {
        Optional<Currency> result = Optional.empty();
        String update = "UPDATE currencies SET code = ?, fullname = ?, sign = ?\n" +
                "WHERE id = ?\n" +
                "RETURNING id, code, fullname, sign";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(update);
        ) {
            preparedStatement.setString(1, entity.getCode());
            preparedStatement.setString(2, entity.getName());
            preparedStatement.setString(3, entity.getSign());
            preparedStatement.setInt(4, entity.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    String code = resultSet.getString("code");
                    String name = resultSet.getString("fullname");
                    String sign = resultSet.getString("sign");
                    Currency currency = new Currency(id, code, name, sign);
                    LOGGER.info("model has been changed with new parameters: {}", entity);
                    result = Optional.of(currency);
                    return result;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error during update of record with ID: {} ", entity.getId(), e);
            throw new DatabaseException("Failed to update currencies: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> result = new ArrayList<>();
        String get = "SELECT id, code, fullname, sign FROM currencies";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(get);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String fullname = resultSet.getString("fullname");
                String sign = resultSet.getString("sign");
                result.add(new Currency(id, code, fullname, sign));
                LOGGER.info("get a record  id {} code {} fullname {} sign {}", id, code, fullname, sign);
            }

        } catch (SQLException e) {
            LOGGER.error("Error retrieving all currencies from database", e);
            throw new DatabaseException("Failed to retrieve currencies " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Optional<Currency> findById(Integer id) {
        Optional<Currency> result = Optional.empty();
        String find = "SELECT id, code, fullname, sign FROM currencies WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(find)
        ) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    result = Optional.of(new Currency(
                            resultSet.getInt("id"),
                            resultSet.getString("code"),
                            resultSet.getString("fullname"),
                            resultSet.getString("sign")));
                    return result;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.error("Error retrieving currencies by id from database", sqlException);
            throw new DatabaseException("Failed to retrieve currencies by id: " + sqlException.getMessage(), sqlException);
        }
        return result;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        Optional<Currency> result = Optional.empty();
        String find = "SELECT id, code, fullname, sign FROM currencies WHERE code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(find)
        ) {
            preparedStatement.setString(1, code);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    result = Optional.of(new Currency(
                            resultSet.getInt("id"),
                            resultSet.getString("code"),
                            resultSet.getString("fullname"),
                            resultSet.getString("sign")));
                    return result;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving currencies by code from database", e);
            throw new DatabaseException("Failed to retrieve currencies by code: " + e.getMessage(), e);
        }
        return result;
    }
}
