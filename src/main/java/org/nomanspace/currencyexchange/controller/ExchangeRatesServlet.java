package org.nomanspace.currencyexchange.controller;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityAlreadyExistsException;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.service.ExchangeRateService;

import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRatesServlet.class);


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for exchangeRates.");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try {
            List<ExchangeRate> exchangeRates = exchangeRateService.getAllExchangeRates();
            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, exchangeRates);
            printWriter.flush();
        } catch (DatabaseException e) {
            LOGGER.error("Data base error on findAll ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error on findAll");
        } catch (IOException e) {
            LOGGER.error("Error writing HTTP response IOException ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error writing HTTP response");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing POST request for /exchangeRate/*.");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            String baseCurrencyCode = req.getParameter("baseCurrencyCode");
            String targetCurrencyCode = req.getParameter("targetCurrencyCode");
            String sRate = req.getParameter("rate");

            if (validateReqParams(baseCurrencyCode, targetCurrencyCode, sRate)) {
                //throw new InvalidDataException("Missing form fields");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing form fields");
                return;
            }
            BigDecimal rate;
            try {
                rate = new BigDecimal(sRate);
            } catch (NumberFormatException e) {
                //это же нормально, что ловлю один тип ошибки, но выкидываю другой, т.к. мне нужно просто сообщить
                //что формат неверный?
                //может стоит добавить регекс на проверку курса?
                //5. Если строка не число — поймать NumberFormatException и вернуть 400.
                //throw new InvalidDataException("Incorrect value in rate field");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Incorrect value in rate field");
                return;
            }
            //ExchangeRate exchangeRate = new ExchangeRate(); // тут нужен мапинг дто?
            ExchangeRateRequestDTO dto = new ExchangeRateRequestDTO();
            dto.setBaseCurrencyCode(baseCurrencyCode.toUpperCase(Locale.US));
            dto.setTargetCurrencyCode(targetCurrencyCode.toUpperCase(Locale.US));
            dto.setExchangeRate(rate);

            Optional<ExchangeRate> createdPair = exchangeRateService.createNewExchangeRate(dto);
            if (createdPair.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incorrect value in rate field");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_CREATED);
            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, createdPair.get());
            printWriter.flush();

            //6. Собрать ExchangeRateRequestDTO и передать в сервис.
            //7. Если создание успешно — вернуть JSON и явно поставить 201 Created.

        } catch (EntityNotFoundException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (DatabaseException e) {
            LOGGER.error("Data base error on findByCode ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        } catch (IOException e) {
            LOGGER.error("Error writing HTTP response ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) {
        try {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            resp.setStatus(status);
            JsonUtil.toJson(resp.getWriter(), Map.of("message", message));
        } catch (IOException e) {
            LOGGER.error("Failed to send error response", e);
        }
    }

    private boolean validateReqParams(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        return baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                rate == null || rate.isBlank();
    }
}
