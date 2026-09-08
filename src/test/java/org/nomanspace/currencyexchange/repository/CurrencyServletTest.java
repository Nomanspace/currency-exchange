package org.nomanspace.currencyexchange.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.nomanspace.currencyexchange.controller.handlers.CurrencyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.nomanspace.currencyexchange.model.Currency;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrencyServletTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyServletTest.class);

    @Mock
    private HttpServletResponse mockResp;

    @Mock
    private HttpServletRequest mockReq;

    @Mock
    private CurrencyRepository mockRepo;

    @InjectMocks
    private CurrencyServlet currencyServlet;


    @Test
    void testDoGetConcreteCurrency() throws Exception {

        Currency expectedCur = new Currency("RUB", "Russian rubl", "P");

        //Arrange
        //CurrencyServlet currencyServlet = new CurrencyServlet(mockRepo);

        //when(mockReq.getPathInfo()).thenReturn("/currency/RUB");
        when(mockReq.getAttribute("currency")).thenReturn("RUB");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(mockResp.getWriter()).thenReturn(printWriter);
        when(mockRepo.findByCode("RUB")).thenReturn(Optional.of(expectedCur));

        currencyServlet.doGet(mockReq, mockResp);

        //Act
        //получить какой-то result = ;
        //LOGGER.info("Request received: {} {}", req.getMethod(), req.getRequestURI());

        String resultCode = stringWriter.toString();


        //Assert
        //assertEquals(resultCode, "RUB");
        assertTrue(resultCode.contains("\"code\":\"RUB\""));


    }


}
