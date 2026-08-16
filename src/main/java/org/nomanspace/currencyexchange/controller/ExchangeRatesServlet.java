package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ExchangeRatesServlet implements Handler {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRatesServlet.class);

    public ExchangeRatesServlet(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for exchangeRates.");
        List<ExchangeRate> exchangeRates = exchangeRateService.getAllExchangeRates();
        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, exchangeRates);
        printWriter.flush();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing POST request for exchangeRates.");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String sRate = req.getParameter("rate");

        if (validateReqParams(baseCurrencyCode, targetCurrencyCode, sRate)) {
            throw new InvalidDataException("Missing form fields");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(sRate);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Incorrect value in rate field");
        }

        ExchangeRateRequestDTO dto = new ExchangeRateRequestDTO();
        dto.setBaseCurrencyCode(baseCurrencyCode.toUpperCase(Locale.US));
        dto.setTargetCurrencyCode(targetCurrencyCode.toUpperCase(Locale.US));
        dto.setExchangeRate(rate);

        Optional<ExchangeRate> createdPair = exchangeRateService.createNewExchangeRate(dto);
        if (createdPair.isEmpty()) {
            throw new DatabaseException("Failed to create exchange rate");
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, createdPair.get());
        printWriter.flush();
    }

    private boolean validateReqParams(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        return baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                rate == null || rate.isBlank();
    }
}