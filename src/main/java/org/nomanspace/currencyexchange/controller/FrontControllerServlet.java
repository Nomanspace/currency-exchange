package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.exception.ApiException;
import org.nomanspace.currencyexchange.exception.EntityNotFoundException;
import org.nomanspace.currencyexchange.exception.MethodNotAllowedException;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/*")
public class FrontControllerServlet extends HttpServlet {
    private CurrencyRepository currencyRepository;
    private Map<String, Handler> servletDict;
    HandlersRegistry handlersRegistry;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontControllerServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        handlersRegistry = (HandlersRegistry) servletContext.getAttribute("handlersRegistry");
        servletDict = handlersRegistry.getHandlers();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        try {


       /* if (req.getPathInfo() == null) {
            //throw new InvalidDataException("Currency code is missing");
            //sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing");
            //LOGGER.info("Currency code is missing");
            return;
        }*/
            String[] uri = req.getPathInfo().split("/");
            String apiUri = uri[1];
            Handler handler = servletDict.get(apiUri);
            if (handler == null) {
                throw new EntityNotFoundException("path not found: " + req.getPathInfo());
                //return; не нужен, так как в try catch блоке ошибка улетит сразу в catch
            }
            String method = req.getMethod();


            switch (method) {
                case "GET" -> handler.doGet(req, resp);
                case "POST" -> handler.doPost(req, resp);
                case "PATCH" -> handler.doPatch(req, resp);
                default -> throw new MethodNotAllowedException();
            }

        } catch (ApiException e) {
            LOGGER.info(e.getMessage(), e); //ожидаемый бизнес ответ,
            // лог можно не писать, но я хочу оставить для себя
            sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
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
