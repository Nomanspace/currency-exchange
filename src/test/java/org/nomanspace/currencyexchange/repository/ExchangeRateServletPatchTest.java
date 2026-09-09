package org.nomanspace.currencyexchange.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nomanspace.currencyexchange.controller.handlers.ExchangeRateServlet;
import org.nomanspace.currencyexchange.dto.ExchangeRateRequestDTO;
import org.nomanspace.currencyexchange.exception.InvalidDataException;
import org.nomanspace.currencyexchange.model.ExchangeRate;
import org.nomanspace.currencyexchange.service.ExchangeRateService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServletPatchTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Test
    void doPatchThrowsBadRequestForEmptyBody() throws Exception {
        ExchangeRateServlet servlet = servletWithBody("");

        assertThrows(InvalidDataException.class, () -> servlet.doPatch(request, response));

        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void doPatchThrowsBadRequestForInvalidRate() throws Exception {
        ExchangeRateServlet servlet = servletWithBody("rate=17=abc");

        assertThrows(InvalidDataException.class, () -> servlet.doPatch(request, response));

        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void doPatchThrowsBadRequestForDuplicateRate() throws Exception {
        ExchangeRateServlet servlet = servletWithBody("rate=17&rate=18");

        assertThrows(InvalidDataException.class, () -> servlet.doPatch(request, response));

        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void doPatchDecodesAndUpdatesValidRate() throws Exception {
        ExchangeRateServlet servlet = servletWithBody("rate=1%2E5");
        StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(exchangeRateService.updateExistExchangeRate(org.mockito.ArgumentMatchers.any(ExchangeRateRequestDTO.class)))
                .thenReturn(new ExchangeRate());

        servlet.doPatch(request, response);

        ArgumentCaptor<ExchangeRateRequestDTO> dtoCaptor = ArgumentCaptor.forClass(ExchangeRateRequestDTO.class);
        verify(exchangeRateService).updateExistExchangeRate(dtoCaptor.capture());
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("USD", dtoCaptor.getValue().getBaseCurrencyCode());
        assertEquals("RUB", dtoCaptor.getValue().getTargetCurrencyCode());
        assertEquals(new BigDecimal("1.5"), dtoCaptor.getValue().getExchangeRate());
    }

    private ExchangeRateServlet servletWithBody(String body) throws Exception {
        when(request.getAttribute("exchangeRate")).thenReturn("USDRUB");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
        return new ExchangeRateServlet(exchangeRateService);
    }
}
