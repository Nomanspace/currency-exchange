package org.nomanspace.currencyexchange.service;

import org.nomanspace.currencyexchange.dto.CurrencyDTO;
import org.nomanspace.currencyexchange.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyService {
    List<Currency> getAllCurrencies();

    Optional<Currency> getCurrencyByCode(String code);

    Optional<Currency> addNewCurrency(CurrencyDTO currencyDTO);
}
