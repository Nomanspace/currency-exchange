package org.nomanspace.currencyexchange.datasource;


public class DatabaseConfigProvider {

    public DatabaseConfig getConfig() {

        String url = System.getenv("DB_URL");
        if (url == null) {
            url = "jdbc:postgresql://localhost:5432/currencyexchange?useUnicode=true&characterEncoding=UTF-8";
        }

        String username = System.getenv("DB_USER");
        if (username == null ) {
            username = "postgres";
        }

        String pass = System.getenv("DB_PASSWORD");
        if (pass == null) {
            pass = "postgres";
        }

        return new DatabaseConfig(url, username, pass);
    }
}
