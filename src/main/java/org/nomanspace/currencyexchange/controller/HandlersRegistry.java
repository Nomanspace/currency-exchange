package org.nomanspace.currencyexchange.controller;

import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.service.ExchangeRateService;

import java.util.HashMap;
import java.util.Map;

public class HandlersRegistry {

    private Map<String, Handler> handlers;
    private CurrencyRepository currencyRepository;
    private ExchangeRateService exchangeRateService;

    public Map<String, Handler> getHandlers() {
        return handlers;
    }

    public HandlersRegistry(CurrencyRepository currencyRepository, ExchangeRateService exchangeRateService) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateService = exchangeRateService;
        handlers = fill();
    }

    private HashMap<String, Handler> fill() {
        return new HashMap<>(Map.of(
                "currencies", new CurrenciesServlet(currencyRepository),
                "currency", new CurrencyServlet(currencyRepository),
                "exchangeRates", new ExchangeRatesServlet(exchangeRateService),
                "exchangeRate", new ExchangeRateServlet(exchangeRateService),
                "exchange", new ExchangeServlet(exchangeRateService)
        ));
    }
}