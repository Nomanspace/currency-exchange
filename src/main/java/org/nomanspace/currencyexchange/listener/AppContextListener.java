package org.nomanspace.currencyexchange.listener;


import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;
import org.nomanspace.currencyexchange.controller.HandlersRegistry;
import org.nomanspace.currencyexchange.datasource.DataSource;
import org.nomanspace.currencyexchange.datasource.DatabaseConfig;
import org.nomanspace.currencyexchange.datasource.DatabaseConfigProvider;
import org.nomanspace.currencyexchange.datasource.impl.HikariDataSourceImpl;
//import org.nomanspace.currencyexchange.datasource.impl.SavageDataSourceImpl;
import org.nomanspace.currencyexchange.repository.CurrencyRepository;
import org.nomanspace.currencyexchange.repository.ExchangeRateRepository;
import org.nomanspace.currencyexchange.repository.impl.CurrencyRepositoryImpl;
import org.nomanspace.currencyexchange.repository.impl.ExchangeRateRepositoryImpl;
import org.nomanspace.currencyexchange.service.ExchangeRateService;
import org.nomanspace.currencyexchange.service.impl.ExchangeRateServiceImpl;

@WebListener
public class AppContextListener implements ServletContextListener {
    private DataSource dataSource;
    ServletContext stcx;

    /**
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseConfig dc = new DatabaseConfigProvider().getConfig();
        Flyway flyway = Flyway.configure().
                dataSource(dc.getUrl(), dc.getUsername(), dc.getPassword()).
                locations("classpath:db/migration").
                baselineOnMigrate(true).
                load();
        flyway.migrate();
        //DataSource dataSource = new SavageDataSourceImpl(dc);
        dataSource = new HikariDataSourceImpl(dc);
        CurrencyRepository currencyRepositoryImpl = new CurrencyRepositoryImpl(dataSource);
        ExchangeRateRepository exchangeRateRepositoryImpl = new ExchangeRateRepositoryImpl(dataSource);
        ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateRepositoryImpl, currencyRepositoryImpl);
        HandlersRegistry handlersRegistry = new HandlersRegistry(currencyRepositoryImpl, exchangeRateService);
        stcx = sce.getServletContext();
        stcx.setAttribute("currencyRepository", currencyRepositoryImpl);
        stcx.setAttribute("exchangeRateService", exchangeRateService);
        stcx.setAttribute("handlersRegistry", handlersRegistry);

    }

    /**
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        dataSource.closeConnectionPool();
        ServletContextListener.super.contextDestroyed(sce);
    }
}
