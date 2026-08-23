package org.nomanspace.currencyexchange.service;

import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeRateService {
    public List<ExchangeRate> getAllExchangeRates();

    public ExchangeRate getExchangeRateByCode(String baseCode, String targetCode);

    public ExchangeRate createNewExchangeRate(ExchangeRateRequestDTO exchangeRateRequestDTO);

    public ExchangeRate updateExistExchangeRate(ExchangeRateRequestDTO exchangeRateRequestDTO);

    public ExchangeResponseDTO convertCurrency(String fromCode, String toCode, BigDecimal amount);
}
