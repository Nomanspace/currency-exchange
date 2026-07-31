package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyRepository currencyRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyServlet.class);
    private static final Pattern IS_CODE_CORRECT = Pattern.compile("^[A-Z]{3}$");

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /currency/*.");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        /*
        Отлавливать ошибки стоит только при исключительных ситуациях, так как стек трейс тяжелый стринг объект и его
        будет занимать время ответа.
        что бы это обойти, можно сразу сгенерировать кастомную ошибку и отправить ее пользователю
        действительно важные ошибки отлавливаем в логгер со стек трейсом.
        наружу стек трейс не отдаем
        только кастомное описание по тз к ендпоинту.
        */

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                //throw new InvalidDataException("Currency code is missing");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing");
                LOGGER.info("Currency code is missing");
                return;
            }

            String code = pathInfo.substring(1).toUpperCase(Locale.UK);
            Matcher matcher = IS_CODE_CORRECT.matcher(code);
            boolean isURLRight = matcher.matches();
            //придумать как валидировать данные из url path.
            //bollean isURLRight = code.matches("^[A-Z]{3}$");
            if (!isURLRight) {
                //throw new InvalidDataException("Incorrect currency code format");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Incorrect currency code format");
                LOGGER.info("Incorrect currency code format: {}", code);
                return;
            }

            Optional<Currency> currencyOptional = currencyRepository.findByCode(code);
            if (currencyOptional.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
                return;
            }
            Currency currency = currencyOptional.get();

            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, currency);
            printWriter.flush();

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
            resp.setStatus(status);
            resp.setContentType("application/json");
            JsonUtil.toJson(resp.getWriter(), Map.of("message", message));
        } catch (IOException e) {
            LOGGER.error("Failed to send error response", e);
        }
    }
}
