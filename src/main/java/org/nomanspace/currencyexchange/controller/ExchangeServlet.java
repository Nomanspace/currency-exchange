package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServlet.class);

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");

    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for /exchangeRate/*.");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            //req.getQueryString()
            String from = req.getParameter("from");
            String to = req.getParameter("to");
            String sAmount = req.getParameter("amount");
            //PrintWriter out = resp.getWriter();
            LOGGER.info("Parameters: from={}, to={}, amount={}", from, to, sAmount);
            // Формируем JSON (простой пример)
            //out.write("{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
            //out.flush();
            if (validateReqParams(from, to, sAmount)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing form fields");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(sAmount);
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Incorrect value in amount field");
                return;
            }

            /*ExchangeRequestDTO exchangeRequestDTO = new ExchangeRequestDTO();
            exchangeRequestDTO.setBaseCurrencyCode(from);
            exchangeRequestDTO.setTargetCurrencyCode(to);
            exchangeRequestDTO.setAmount(amount);*/

            ExchangeResponseDTO dto = exchangeRateService.convertCurrency(from, to, amount).
                    orElseThrow(() -> new EntityNotFoundException("ExchangeRate not found"));

            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter printWriter = resp.getWriter();
            JsonUtil.toJson(printWriter, dto);
            printWriter.flush();

        } catch (EntityNotFoundException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DatabaseException e) {
            LOGGER.error("Data base error on findByCode ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        } catch (IOException e) {
            LOGGER.error("Error writing HTTP response ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "База данных недоступна");
        }
    }

    private boolean validateReqParams(String baseCurrencyCode, String targetCurrencyCode, String amount) {
        return baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                amount == null || amount.isBlank();
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
