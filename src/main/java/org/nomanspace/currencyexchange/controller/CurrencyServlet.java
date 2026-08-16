package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyServlet implements Handler {
    private CurrencyRepository currencyRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyServlet.class);
    private static final Pattern IS_CODE_CORRECT = Pattern.compile("^[A-Z]{3}$");

    public CurrencyServlet(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /currency/*.");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            LOGGER.info("Currency code is missing");
            throw new InvalidDataException("Currency code is missing");
        }

        String code = pathInfo.substring(1).toUpperCase(Locale.UK);
        Matcher matcher = IS_CODE_CORRECT.matcher(code);
        boolean isURLRight = matcher.matches();
        if (!isURLRight) {
            LOGGER.info("Incorrect currency code format: {}", code);
            throw new InvalidDataException("Incorrect currency code format");
        }

        Optional<Currency> currencyOptional = currencyRepository.findByCode(code);
        if (currencyOptional.isEmpty()) {
            throw new EntityNotFoundException("Currency not found");
        }
        Currency currency = currencyOptional.get();

        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, currency);
        printWriter.flush();
    }
}