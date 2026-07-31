package org.nomanspace.currencyexchange.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import org.nomanspace.currencyexchange.datasource.DatabaseConfig;
import org.nomanspace.currencyexchange.datasource.DatabaseConfigProvider;
import org.nomanspace.currencyexchange.datasource.SavageDataSourceImpl;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.repository.impl.CurrencyRepositoryImpl;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class CurrencyRepositoryImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRepositoryImplTest.class);
    CurrencyRepository currencyRepositoryImpl;
    DatabaseConfig databaseConfig;

    @BeforeEach
    void init() {
        databaseConfig = new DatabaseConfigProvider().getConfig();
        currencyRepositoryImpl = new CurrencyRepositoryImpl(new SavageDataSourceImpl(databaseConfig));
    }

    @Test
    void findAll() throws SQLException {
        List<Currency> currencies = currencyRepositoryImpl.findAll();
        assertNotNull(currencies);
        assertEquals(4, currencies.size());
    }

}
