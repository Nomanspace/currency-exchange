package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.math.BigDecimal;

public class ExchangeServlet implements Handler {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServlet.class);

    public ExchangeServlet(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /exchange.");

        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String sAmount = req.getParameter("amount");
        LOGGER.info("Parameters: from={}, to={}, amount={}", from, to, sAmount);

        if (validateReqParams(from, to, sAmount)) {
            throw new InvalidDataException("Missing form fields");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(sAmount);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Incorrect value in amount field");
        }

        ExchangeResponseDTO dto = exchangeRateService.convertCurrency(from, to, amount)
                .orElseThrow(() -> new EntityNotFoundException("ExchangeRate not found"));

        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, dto);
        printWriter.flush();
    }

    private boolean validateReqParams(String baseCurrencyCode, String targetCurrencyCode, String amount) {
        return baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                amount == null || amount.isBlank();
    }
}