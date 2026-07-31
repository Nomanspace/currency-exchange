package org.nomanspace.currencyexchange.repository;

import org.nomanspace.currencyexchange.model.ExchangeRate;

import java.sql.SQLException;
import java.util.Optional;

public interface ExchangeRateRepository extends Repository<ExchangeRate, Integer> {

    Optional<ExchangeRate> findByCurrencyCode(String baseCode, String targetCode);
}
