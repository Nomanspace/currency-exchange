package org.nomanspace.currencyexchange.service.impl;


import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.repository.ExchangeRateRepository;
import org.nomanspace.currencyexchange.service.ExchangeRateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;


public class ExchangeRateServiceImpl implements ExchangeRateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServiceImpl.class);

    ExchangeRateRepository exchangeRateRepository;
    CurrencyRepository currencyRepository;


    public ExchangeRateServiceImpl(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }


    @Override
    public List<ExchangeRate> getAllExchangeRates() {
        List<ExchangeRate> result;
        result = exchangeRateRepository.findAll();
        return result;
    }


    @Override
    public ExchangeRate getExchangeRateByCode(String baseCode, String targetCode) {
        return exchangeRateRepository.findByCurrencyCode(baseCode, targetCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Exchange rate pair %s/%s not found", baseCode, targetCode)));
    }


    @Override
    public ExchangeRate createNewExchangeRate(ExchangeRateRequestDTO dto) {
        String baseCurrencyCode;
        String targetCurrencyCode;
        Currency baseCurrency;
        Currency targetCurrency;
        baseCurrencyCode = dto.getBaseCurrencyCode();
        targetCurrencyCode = dto.getTargetCurrencyCode();
        baseCurrency = currencyRepository.findByCode(baseCurrencyCode).orElseThrow(() -> new EntityNotFoundException("Base Currency not found: " + dto.getBaseCurrencyCode()));
        targetCurrency = currencyRepository.findByCode(targetCurrencyCode).orElseThrow(() -> new EntityNotFoundException("Target Currency not found: " + dto.getTargetCurrencyCode()));
        return exchangeRateRepository.save(new ExchangeRate(baseCurrency, targetCurrency, dto.getExchangeRate()))
                .orElseThrow(() -> new DatabaseException("Exchange rate was not created"));
    }


    @Override
    public ExchangeRate updateExistExchangeRate(ExchangeRateRequestDTO dto) {
        String baseCurrencyCode = dto.getBaseCurrencyCode();
        String targetCurrencyCode = dto.getTargetCurrencyCode();
        BigDecimal rateToUpdate = dto.getExchangeRate();
        ExchangeRate tempPairToUpdate = exchangeRateRepository.findByCurrencyCode(baseCurrencyCode, targetCurrencyCode).
                orElseThrow(() -> new EntityNotFoundException(String.format("Exchange rate pair %s/%s not found", baseCurrencyCode, targetCurrencyCode)));
        LOGGER.info("found exist pair to update: {}", tempPairToUpdate);
        tempPairToUpdate.setRate(rateToUpdate);
        return exchangeRateRepository.update(tempPairToUpdate).orElseThrow(() -> new DatabaseException("Exchange rate was not update"));
    }

    @Override
    public ExchangeResponseDTO convertCurrency(String fromCode, String toCode, BigDecimal amount) {
        //BigDecimal convertedAmount = null;
        //BigDecimal rate = null;
        LOGGER.info("search pair {}/{}", fromCode, toCode);
        /*Optional<ExchangeRate> tempExRate = getExchangeRateByCode(fromCode, toCode);
        if (tempExRate.isPresent()) {
            rate = tempExRate.get().getRate();
            convertedAmount = amount.multiply(rate);
            ExchangeResponseDTO response = new ExchangeResponseDTO();
            result = Optional.of();
        }*/
        Optional<ExchangeResponseDTO> result = getStraightExchange(fromCode, toCode, amount);
        if (result.isPresent()) {
            return result.get();
        }

        result = getRevertExchange(fromCode, toCode, amount);
        if (result.isPresent()) {
            return result.get();
        }

        result = getCrossExchange(fromCode, toCode, amount);
        if (result.isPresent()) {
            return result.get();
        }

        throw new EntityNotFoundException(String.format("Exchange rate pair %s/%s not found", fromCode, toCode));
        /*
        return getStraightExchange(fromCode, toCode, amount)
        .or(() -> getRevertExchange(fromCode, toCode, amount))
        .or(() -> getCrossExchange(fromCode, toCode, amount));
         */
    }

    private Optional<ExchangeResponseDTO> getCrossExchange(String fromCode, String toCode, BigDecimal amount) {
        Optional<ExchangeRate> targetExRate = exchangeRateRepository.findByCurrencyCode("USD", toCode);
        Optional<ExchangeRate> baseExRate = exchangeRateRepository.findByCurrencyCode("USD", fromCode);
        if (targetExRate.isPresent() && baseExRate.isPresent()) {
            BigDecimal crossRate = targetExRate.get().getRate().divide(baseExRate.get().getRate(), 6, RoundingMode.HALF_EVEN);
            return Optional.of(new ExchangeResponseDTO(
                    baseExRate.get().getTargetCurrency(),
                    targetExRate.get().getTargetCurrency(),
                    crossRate,
                    amount,
                    amount.multiply(crossRate).
                            setScale(2, RoundingMode.HALF_EVEN)
            ));
        }
        return Optional.empty();
    }

    private Optional<ExchangeResponseDTO> getRevertExchange(String fromCode, String toCode, BigDecimal amount) {
        /*return getExchangeRateByCode(toCode, fromCode).map(pair ->
                new ExchangeResponseDTO(
                        pair.getBaseCurrency(),
                        pair.getTargetCurrency(),
                        pair.getRate(),
                        amount,
                        amount.divide(pair.getRate(), RoundingMode.HALF_EVEN)
                ));*/
        Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findByCurrencyCode(toCode, fromCode);
        //ExchangeRate tempRate = exchangeRate.orElseThrow(() -> new RuntimeException(String.format("Exchange rate pair %s/%s not found", toCode, fromCode)));
        if (exchangeRate.isPresent()) {
            ExchangeRate tempRate = exchangeRate.get();
            BigDecimal divisor = tempRate.getRate();
            BigDecimal reverseRate = BigDecimal.valueOf(1).divide(divisor, 6, RoundingMode.HALF_EVEN);
            BigDecimal quotient = amount.divide(divisor, 6, RoundingMode.HALF_EVEN);
            return Optional.of(new ExchangeResponseDTO(tempRate.getTargetCurrency(),
                    tempRate.getBaseCurrency(), reverseRate, amount, quotient));
        }
        return Optional.empty();
    }

    private Optional<ExchangeResponseDTO> getStraightExchange(String fromCode, String toCode, BigDecimal amount) {
        Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findByCurrencyCode(fromCode, toCode);
        if (exchangeRate.isPresent()) {
            ExchangeRate pair = exchangeRate.get();
            return Optional.of(new ExchangeResponseDTO(
                    pair.getBaseCurrency(),
                    pair.getTargetCurrency(),
                    pair.getRate(),
                    amount,
                    amount.multiply(pair.getRate()).
                            setScale(2, RoundingMode.HALF_EVEN)
            ));
        }
        return Optional.empty();
    }


}
