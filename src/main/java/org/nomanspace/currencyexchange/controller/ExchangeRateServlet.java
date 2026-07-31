package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.*;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServlet.class);
    private static final Pattern IS_CODE_CORRECT = Pattern.compile("^[A-Z]{6}$");

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /exchangeRate/*.");
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
                LOGGER.info("ExchangeRate code is missing");
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
                LOGGER.info("Incorrect ExchangeRate code format: {}", code);
                return;
            }

            String baseCode = code.substring(0, 3);
            String targetCode = code.substring(3, 6);
            //Optional<ExchangeRate> exchangeRateOptional
            ExchangeRate createdPair = exchangeRateService.getExchangeRateByCode(baseCode, targetCode).
                    orElseThrow(() -> new DatabaseException("Exchange rate was not created"));

            /*if (exchangeRateOptional.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "ExchangeRate not found");
                return;
            }*/
            //ExchangeRate createdPair = exchangeRateOptional.get();

            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, createdPair);
            printWriter.flush();

        } catch (DatabaseException e) {
            LOGGER.error("Data base error on findByCode ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        } catch (IOException e) {
            LOGGER.error("Error writing HTTP response ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        }
    }


    /*@Override
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
    }*/


    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing Patch request for /exchangeRate/*.");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing");
                LOGGER.info("ExchangeRate code is missing");
                return;
            }

            String code = pathInfo.substring(1).toUpperCase(Locale.UK);
            Matcher matcher = IS_CODE_CORRECT.matcher(code);
            boolean isURLRight = matcher.matches();

            if (!isURLRight) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Incorrect currency code format");
                LOGGER.info("Incorrect ExchangeRate code format: {}", code);
                return;
            }

            String sRate = req.getParameter("rate");

            if (validateReqParams(sRate)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing form fields");
                return;
            }

            BigDecimal rate;

            try {
                rate = new BigDecimal(sRate);
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Incorrect value in rate field");
                return;
            }

            String baseCode = code.substring(0, 3);
            String targetCode = code.substring(3, 6);
            ExchangeRateRequestDTO dto = new ExchangeRateRequestDTO();
            dto.setBaseCurrencyCode(baseCode);
            dto.setTargetCurrencyCode(targetCode);
            dto.setExchangeRate(rate);
            ExchangeRate createdPair = exchangeRateService.updateExistExchangeRate(dto).
                    orElseThrow(() -> new DatabaseException("Exchange rate was not created"));

            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, createdPair);
            printWriter.flush();

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

    private boolean validateReqParams(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        return baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                rate == null || rate.isBlank();
    }

    private boolean validateReqParams(String rate) {
        return rate == null || rate.isBlank();
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
