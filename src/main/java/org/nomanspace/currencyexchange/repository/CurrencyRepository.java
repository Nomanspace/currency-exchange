package org.nomanspace.currencyexchange.repository;

import org.nomanspace.currencyexchange.model.Currency;

import java.util.Optional;

public interface CurrencyRepository extends Repository<Currency, Integer> {
    Optional<Currency> findByCode(String code);
}
