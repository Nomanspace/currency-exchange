package org.nomanspace.currencyexchange.repository.impl;

import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityAlreadyExistsException;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.datasource.DataSource;
import org.nomanspace.currencyexchange.repository.ExchangeRateRepository;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ExchangeRateRepositoryImpl implements ExchangeRateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateRepositoryImpl.class);
    DataSource savageDataSourceImpl;

    public ExchangeRateRepositoryImpl(DataSource dataSource) {
        this.savageDataSourceImpl = dataSource;
    }

    @Override
    public Optional<ExchangeRate> save(ExchangeRate rateToSave) {
        Optional<ExchangeRate> result = Optional.empty();
        String saveQuery = "INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, ExchangeRate) " +
                "VALUES (?,?,?) " +
                "RETURNING id, BaseCurrencyId, TargetCurrencyId";
        try (Connection connection = savageDataSourceImpl.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(saveQuery)
        ) {
            preparedStatement.setInt(1, rateToSave.getBaseCurrency().getId());
            preparedStatement.setInt(2, rateToSave.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, rateToSave.getRate());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    rateToSave.setId(id);
                    result = Optional.of(rateToSave);
                    return result;
                }
            }
            return result;
        } catch (SQLException e) {
            if (PSQLState.UNIQUE_VIOLATION.getState().equals(e.getSQLState())) {
                throw new EntityAlreadyExistsException("Pair with code " + rateToSave.getBaseCurrency() + " and " + rateToSave.getTargetCurrency() + " already exists");
            }
            LOGGER.error("An error occurred when adding a pair to the base ", e);
            throw new DatabaseException("An error occurred when adding a pair to the base : " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        List<ExchangeRate> result = new ArrayList<>();
        String get = "SELECT\n" +
                "er.id AS rate_id,\n" +
                "er.ExchangeRate AS rate,\n" +
                " " +
                "bc.id AS base_id,\n" +
                "bc.code AS base_code,\n" +
                "bc.fullName AS base_name,\n" +
                "bc.sign AS base_sign,\n" +
                "\n" +
                "tc.id AS target_id,\n" +
                "tc.code AS target_code,\n" +
                "tc.fullName AS target_name,\n" +
                "tc.sign AS target_sign\n" +
                "FROM\n" +
                "ExchangeRates er\n" +
                "JOIN\n" +
                "Currencies bc ON er.BaseCurrencyId = bc.id\n" +
                "JOIN\n" +
                "Currencies tc ON er.TargetCurrencyId = tc.id";
        try (Connection connection = savageDataSourceImpl.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(get);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {

                Currency baseCurrency = new Currency(
                        resultSet.getInt("base_id"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_name"),
                        resultSet.getString("base_sign")
                );


                Currency targetCurrency = new Currency(
                        resultSet.getInt("target_id"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_name"),
                        resultSet.getString("target_sign")
                );

                ExchangeRate currentExchangeRate = new ExchangeRate(
                        resultSet.getInt("rate_id"),
                        baseCurrency,
                        targetCurrency,
                        resultSet.getBigDecimal("rate")
                );

                LOGGER.info("get a ExchangeRate id {} BaseCurrencyId {} TargetCurrencyId {} ExchangeRate {}", currentExchangeRate.getId(), baseCurrency.getId(), targetCurrency.getId(), currentExchangeRate.getRate());
                result.add(currentExchangeRate);
            }

        } catch (SQLException e) {
            LOGGER.error("Something get wrong in findAll method", e);
            throw new DatabaseException("Something get wrong in findAll method : " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Optional<ExchangeRate> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public boolean delete(Integer id) {
        String deleteQuery = "DELETE FROM ExchangeRates WHERE id = ?";
        int rowsAffected = 0;

        try (Connection connection = savageDataSourceImpl.getConnection();
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
            throw new DatabaseException("An error occurred when delete the pair in base : " + e.getMessage(), e);
        }
        return rowsAffected > 0;
    }

    @Override
    public Optional<ExchangeRate> update(ExchangeRate entity) {
        Optional<ExchangeRate> result = Optional.empty();
        String saveQuery = "UPDATE ExchangeRates SET ExchangeRate = ?\n" +
                "WHERE id = ?\n" +
                "RETURNING id, ExchangeRate";
        try (Connection connection = savageDataSourceImpl.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(saveQuery)
        ) {
            preparedStatement.setBigDecimal(1, entity.getRate());
            preparedStatement.setInt(2, entity.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    BigDecimal rate = resultSet.getBigDecimal("ExchangeRate");
                    entity.setId(id);
                    entity.setRate(rate);
                    result = Optional.of(entity);
                    return result;
                }
            }
            return result;
        } catch (SQLException e) {
            LOGGER.error("An error occurred when update rate ", e);
            throw new DatabaseException("An error occurred when update rate : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ExchangeRate> findByCurrencyCode(String baseCode, String targetCode) {
        Optional<ExchangeRate> result = Optional.empty();
        String find = "SELECT ex.id AS id,\n" +
                "ex.basecurrencyid AS base_id,\n" +
                "ex.targetcurrencyid AS target_id,\n" +
                "ex.exchangerate AS exchangerate,\n" +
                "base.id AS base_id,\n" +
                "base.code AS base_code,\n" +
                "base.fullname AS base_name,\n" +
                "base.sign AS base_sign,\n" +
                "target.id AS target_id,\n" +
                "target.code AS target_code,\n" +
                "target.fullname AS target_name,\n" +
                "target.sign AS target_sign\n" +
                "FROM exchangerates AS ex\n" +
                "JOIN\n" +
                "currencies AS base ON ex.basecurrencyid = base.id\n" +
                "JOIN\n" +
                "currencies AS target ON ex.targetcurrencyid = target.id\n" +
                "WHERE base.code = ? and target.code = ?";

        try (Connection connection = savageDataSourceImpl.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(find)
        ) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    Currency baseCurrency = new Currency(
                            resultSet.getInt("base_id"),
                            resultSet.getString("base_code"),
                            resultSet.getString("base_name"),
                            resultSet.getString("base_sign")
                    );


                    Currency targetCurrency = new Currency(
                            resultSet.getInt("target_id"),
                            resultSet.getString("target_code"),
                            resultSet.getString("target_name"),
                            resultSet.getString("target_sign")
                    );

                    result = Optional.of(new ExchangeRate(resultSet.getInt("id"),
                            baseCurrency,
                            targetCurrency,
                            resultSet.getBigDecimal("exchangerate")));
                    return result;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Something get wrong in findByCode method", e);
            throw new DatabaseException("Something get wrong in findByCode method : " + e.getMessage(), e);

        }
        return result;
    }
}
