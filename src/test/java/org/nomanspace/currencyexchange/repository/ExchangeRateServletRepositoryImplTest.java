package org.nomanspace.currencyexchange.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.nomanspace.currencyexchange.datasource.DatabaseConfig;
import org.nomanspace.currencyexchange.datasource.DatabaseConfigProvider;
import org.nomanspace.currencyexchange.datasource.SavageDataSourceImpl;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.repository.impl.ExchangeRateRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateServletRepositoryImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServletRepositoryImplTest.class);
    ExchangeRateRepository exchangeRateRepositoryImpl;
    ExchangeRate rateFromOptional;
    DatabaseConfig databaseConfig;

    @BeforeEach
    void init() {
        databaseConfig = new DatabaseConfigProvider().getConfig();
        exchangeRateRepositoryImpl = new ExchangeRateRepositoryImpl(new SavageDataSourceImpl(databaseConfig));
        List<ExchangeRate> exchangeRates = null;
        exchangeRates = exchangeRateRepositoryImpl.findAll();
        rateFromOptional = null;
        //exchangeRateRepositoryImpl.
        if (!exchangeRates.isEmpty()) {
            exchangeRateRepositoryImpl.delete(exchangeRates.get(0).getId());
        }

    }


    @AfterEach
    void cleanup() {
        if (rateFromOptional != null) {
            LOGGER.info("Cleaning up test data. Deleting ExchangeRate with ID: {}", rateFromOptional.getId());
            exchangeRateRepositoryImpl.delete(rateFromOptional.getId());
        } else {
            LOGGER.warn("No saved rate to clean up.");
        }
    }

    @Test
    void save_shouldSaveAndReturnNotNull() {
        Currency usd = new Currency(1, "USD", "United States Dollar", "$");
        Currency eur = new Currency(2, "EUR", "Euro", "€");
        ExchangeRate rateToSave = new ExchangeRate(eur, usd, new BigDecimal("1.5"));
        LOGGER.info("Attempting to save new rate: {}/{} with rate {}", rateToSave.getBaseCurrency().getCode(),
                rateToSave.getTargetCurrency().getCode(),
                rateToSave.getRate());
        Optional<ExchangeRate> result = exchangeRateRepositoryImpl.save(rateToSave);
        assertTrue(result.isPresent(), "Ожидали значение, но объект пуст");
        rateFromOptional = result.get();
        LOGGER.info("Successfully saved. New ID is: {}", this.rateFromOptional.getId());
        assertNotNull(rateFromOptional.getId());
    }

    @Test
    void finAllPair() {
        List<ExchangeRate> exchangeRates = exchangeRateRepositoryImpl.findAll();
        if (!exchangeRates.isEmpty()) {
            System.out.println("Pair quantity " + exchangeRates.size());
            System.out.println(exchangeRates.toString());
            assertNotNull(exchangeRates);
            assertEquals(1, exchangeRates.size());
        } else {
            System.out.println("Nothing to searh from test method ");
            assertEquals(0, exchangeRates.size(), "Nothing to search from test method");
        }

    }

}
