package org.nomanspace.currencyexchange.controller.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.nomanspace.currencyexchange.controller.Handler;
import org.nomanspace.currencyexchange.exception.DatabaseException;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.PrintWriter;
import java.util.List;


public class CurrenciesServlet implements Handler {
    private CurrencyRepository currencyRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrenciesServlet.class);


    public CurrenciesServlet(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());
        LOGGER.debug("Processing GET request for currencies.");

        List<Currency> currencies = currencyRepository.findAll();

        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, currencies);
        printWriter.flush();

        /*catch (DatabaseException e) {
            LOGGER.error("Data base error on findAll ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error writing HTTP response ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }*/
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        if (validateReqParams(code, name, sign)) {
            throw new InvalidDataException("Missing form fields");
        }

        Currency currency = new Currency(code, name, sign);
        Currency saved = currencyRepository.save(currency).
                orElseThrow(() -> new DatabaseException("Failed to save currency"));

        resp.setStatus(HttpServletResponse.SC_CREATED);
        //resp.setContentType("application/json");

        PrintWriter printWriter = resp.getWriter();
        JsonUtil.toJson(printWriter, saved);
        printWriter.flush();


        /*catch (InvalidDataException e) {
            LOGGER.warn("Missing form fields. fields contained space or null value ", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            LOGGER.warn("EntityAlreadyExists ", e);
            sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (DatabaseException e) {
            LOGGER.error("Database operation failed ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error ", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }*/

        /*if (result != null) {
            try {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json");
                PrintWriter printWriter = resp.getWriter();
                JsonUtil.toJson(printWriter, result);
                printWriter.flush();
            } catch (IOException ioException) {
                LOGGER.error("write to json error ", ioException);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }*/
    }

    /*private void sendError(HttpServletResponse resp, int status, String message) {
        try {
            resp.setStatus(status);
            resp.setContentType("application/json");
            JsonUtil.toJson(resp.getWriter(), Map.of("message", message));
        } catch (IOException e) {
            LOGGER.error("Failed to send error response", e);
        }
    }*/

    private boolean validateReqParams(String code, String name, String sign) {
        return code == null || code.isBlank() ||
                name == null || name.isBlank() ||
                sign == null || sign.isBlank();
    }
}
