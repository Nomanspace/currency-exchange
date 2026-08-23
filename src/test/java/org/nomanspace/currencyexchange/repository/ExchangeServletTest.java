package org.nomanspace.currencyexchange.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nomanspace.currencyexchange.controller.handlers.ExchangeServlet;
import org.nomanspace.currencyexchange.dto.ExchangeResponseDTO;
import org.nomanspace.currencyexchange.model.Currency;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 1. Аннотация говорит JUnit использовать расширение Mockito для инициализации моков
@ExtendWith(MockitoExtension.class)
class ExchangeServletTest {

    private static final Logger log = LoggerFactory.getLogger(ExchangeServletTest.class);
    // 2. @Mock создает "пустой" поддельный объект. Он ничего не делает сам по себе.
    // Документация: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    // Мок для сервиса конвертации валют - это зависимость хендлера
    @Mock
    private ExchangeRateService mockExchangeRateService;

    @Test
    void testDoGetReturnsCorrectJson() throws Exception {
        // 3. Создаем реальный объект хендлера, который хотим проверить.
        // Зависимость входит через конструктор, как в проде (HandlersRegistry)
        ExchangeServlet servlet = new ExchangeServlet(mockExchangeRateService);

        // 4. ПОДГОТОВКА (Arrange)

        // 4.1. Мы хотим, чтобы при вызове mockRequest.getParameter("from") вернулось "USD".
        // when() ... thenReturn() програмрует мок на нужное поведение.
        // Документация: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#stubbing
        when(mockRequest.getParameter("from")).thenReturn("USD");
        when(mockRequest.getParameter("to")).thenReturn("EUR");
        when(mockRequest.getParameter("amount")).thenReturn("100");

        // 4.2. Проблема: реальный HttpServletResponse не умеет писать в строку.
        // Нам нужно перехватить то, что сервлет пишет через resp.getWriter().
        // Создаем StringWriter (это реальный класс Java, который пишет текст в память, а не в консоль/сеть)
        StringWriter stringWriter = new StringWriter();
        // Оборачиваем его в PrintWriter (как делает реальный контейнер)
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // 4.3. Программируем мок ответа: когда сервлет вызовет resp.getWriter(),
        // отдать ему наш StringWriter.
        when(mockResponse.getWriter()).thenReturn(printWriter);

        // 4.4. Создаем мок-ответ от сервиса конвертации валют
        // Это объект, который вернет сервис при вызове convertCurrency()
        Currency usd = new Currency();
        usd.setCode("USD");
        usd.setName("United States dollar");
        usd.setSign("$");

        Currency eur = new Currency();
        eur.setCode("EUR");
        eur.setName("Euro");
        eur.setSign("€");

        ExchangeResponseDTO mockResponseDTO = new ExchangeResponseDTO();
        mockResponseDTO.setBaseCurrency(usd);
        mockResponseDTO.setTargetCurrency(eur);
        mockResponseDTO.setRate(new BigDecimal("0.85"));
        mockResponseDTO.setAmount(new BigDecimal("100"));
        mockResponseDTO.setConvertedAmount(new BigDecimal("85.00"));

        // Настраиваем мок сервиса: при вызове convertCurrency с конкретными параметрами
        // вернуть наш мок-объект
        when(mockExchangeRateService.convertCurrency("USD", "EUR", new BigDecimal("100")))
            .thenReturn(mockResponseDTO);

        // 5. ДЕЙСТВИЕ (Act)

        // Вызываем метод хендлера, передавая ему моки.
        servlet.doGet(mockRequest, mockResponse);

        // 6. ПРОВЕРКА (Assert)

        // 6.1. Получаем то, что хендлер записал в нашего StringWriter
        String resultJson = stringWriter.toString();

        // 6.2. Проверяем, что в ответе есть нужные данные
        assertTrue(resultJson.contains("\"convertedAmount\":85.00"));

        // 6.3. (Опционально) Проверяем, что хендлер действительно вызывал нужные методы
        // verify() проверяет факт вызова метода на моке.
        // Документация: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#4
        verify(mockRequest).getParameter("from");
    }
}