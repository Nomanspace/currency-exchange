package org.nomanspace.currencyexchange.controller.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.controller.Handler;
import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.util.FormUrlEncodedParser;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeRateServlet implements Handler {
    private final ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServlet.class);
    private static final Pattern IS_CODE_CORRECT = Pattern.compile("^[A-Z]{6}$");

    public ExchangeRateServlet(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /exchangeRate/*.");

        //String pathInfo = req.getPathInfo();
        String pathInfo = (String) req.getAttribute("exchangeRate");
        if (pathInfo == null) {
            LOGGER.info("ExchangeRate code is missing");
            throw new InvalidDataException("Currency code is missing");
        }

        //String code = pathInfo.substring(1).toUpperCase(Locale.UK);
        String code = pathInfo.toUpperCase(Locale.UK);
        LOGGER.info("code value: {}", code);
        Matcher matcher = IS_CODE_CORRECT.matcher(code);
        boolean isURLRight = matcher.matches();
        if (!isURLRight) {
            LOGGER.info("Incorrect ExchangeRate code format: {}", code);
            throw new InvalidDataException("Incorrect currency code format");
        }

        String baseCode = code.substring(0, 3);
        String targetCode = code.substring(3, 6);
        ExchangeRate exchangeRate = exchangeRateService.getExchangeRateByCode(baseCode, targetCode);

        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, exchangeRate);
        printWriter.flush();
    }

    @Override
    public void doPatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing Patch request for /exchangeRate/*.");
        LOGGER.info("Content-Type: {}", req.getContentType());
        LOGGER.info("Content-Length: {}", req.getContentLength());

        //String pathInfo = req.getPathInfo();
        String pathInfo = (String) req.getAttribute("exchangeRate");
        if (pathInfo == null) {
            LOGGER.info("ExchangeRate code is missing");
            throw new InvalidDataException("ExchangeRate code is missing");
        }

        //String code = pathInfo.substring(1).toUpperCase(Locale.UK);
        String code = pathInfo.toUpperCase(Locale.UK);
        LOGGER.info("code value: {}", code);
        Matcher matcher = IS_CODE_CORRECT.matcher(code);
        boolean isURLRight = matcher.matches();
        if (!isURLRight) {
            LOGGER.info("Incorrect ExchangeRate code format: {}", code);
            throw new InvalidDataException("Incorrect currency code format");
        }

        /*
        String allLinesReq = getReq(req);
        String sRate = getRate(allLinesReq).get("rate");
        LOGGER.info("sRate value: {}", sRate);
        if (validateReqParams(sRate)) {
            throw new InvalidDataException("Missing form fields");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(sRate);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Incorrect value in rate field");
        }
        */
        Map<String, String> formParameters = FormUrlEncodedParser.parse(req.getReader());
        BigDecimal rate = FormUrlEncodedParser.getRequiredDecimal(formParameters, "rate");
        LOGGER.info("rate value: {}", rate);

        String baseCode = code.substring(0, 3);
        String targetCode = code.substring(3, 6);
        ExchangeRateRequestDTO dto = new ExchangeRateRequestDTO();
        dto.setBaseCurrencyCode(baseCode);
        dto.setTargetCurrencyCode(targetCode);
        dto.setExchangeRate(rate);
        ExchangeRate updatedPair = exchangeRateService.updateExistExchangeRate(dto);
                //.orElseThrow(() -> new DatabaseException("Exchange rate was not created"));

        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, updatedPair);
        printWriter.flush();
    }

    /*
    private Map<String,String> getRate(String allLinesReq) {
        Map<String,String> result = new HashMap<>();
        String[] firstSplit = allLinesReq.split("&");
        for (int i = 0; i < firstSplit.length; i++) {
            String[] secondSplit = firstSplit[i].split("=");
            result.put(secondSplit[0],secondSplit[1]);
        }

        return result;
    }

    private String getReq(HttpServletRequest req) throws IOException {
        String result;
        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        result = stringBuilder.toString();
        stringBuilder.setLength(0);

        return result;
    }

    private boolean validateReqParams(String rate) {
        return rate == null || rate.isBlank();
    }
    */
}
