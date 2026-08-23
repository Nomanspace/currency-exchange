package org.nomanspace.currencyexchange.controller.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.controller.Handler;
import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeRateServlet implements Handler {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServlet.class);
    private static final Pattern IS_CODE_CORRECT = Pattern.compile("^[A-Z]{6}$");

    public ExchangeRateServlet(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /exchangeRate/*.");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            LOGGER.info("ExchangeRate code is missing");
            throw new InvalidDataException("Currency code is missing");
        }

        String code = pathInfo.substring(1).toUpperCase(Locale.UK);
        Matcher matcher = IS_CODE_CORRECT.matcher(code);
        boolean isURLRight = matcher.matches();
        if (!isURLRight) {
            LOGGER.info("Incorrect ExchangeRate code format: {}", code);
            throw new InvalidDataException("Incorrect currency code format");
        }

        String baseCode = code.substring(0, 3);
        String targetCode = code.substring(3, 6);
        ExchangeRate exchangeRate = exchangeRateService.getExchangeRateByCode(baseCode, targetCode)
                .orElseThrow(() -> new EntityNotFoundException("Exchange rate not found: " + code));

        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, exchangeRate);
        printWriter.flush();
    }

    @Override
    public void doPatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing Patch request for /exchangeRate/*.");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            LOGGER.info("ExchangeRate code is missing");
            throw new InvalidDataException("Currency code is missing");
        }

        String code = pathInfo.substring(1).toUpperCase(Locale.UK);
        Matcher matcher = IS_CODE_CORRECT.matcher(code);
        boolean isURLRight = matcher.matches();
        if (!isURLRight) {
            LOGGER.info("Incorrect ExchangeRate code format: {}", code);
            throw new InvalidDataException("Incorrect currency code format");
        }

        String sRate = req.getParameter("rate");
        if (validateReqParams(sRate)) {
            throw new InvalidDataException("Missing form fields");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(sRate);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Incorrect value in rate field");
        }

        String baseCode = code.substring(0, 3);
        String targetCode = code.substring(3, 6);
        ExchangeRateRequestDTO dto = new ExchangeRateRequestDTO();
        dto.setBaseCurrencyCode(baseCode);
        dto.setTargetCurrencyCode(targetCode);
        dto.setExchangeRate(rate);
        ExchangeRate updatedPair = exchangeRateService.updateExistExchangeRate(dto)
                .orElseThrow(() -> new DatabaseException("Exchange rate was not created"));

        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, updatedPair);
        printWriter.flush();
    }

    private boolean validateReqParams(String rate) {
        return rate == null || rate.isBlank();
    }
}