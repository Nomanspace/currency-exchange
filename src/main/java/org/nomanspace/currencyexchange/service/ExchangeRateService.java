package org.nomanspace.currencyexchange.service;

import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    public List<ExchangeRate> getAllExchangeRates();

    public Optional<ExchangeRate> getExchangeRateByCode(String baseCode, String targetCode);

    public Optional<ExchangeRate> createNewExchangeRate(ExchangeRateRequestDTO exchangeRateRequestDTO);

    public Optional<ExchangeRate> updateExistExchangeRate(ExchangeRateRequestDTO exchangeRateRequestDTO);

    public Optional<ExchangeResponseDTO> convertCurrency(String fromCode, String toCode, BigDecimal amount);
}
