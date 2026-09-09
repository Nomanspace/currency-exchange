package org.nomanspace.currencyexchange.util;

import org.nomanspace.currencyexchange.exception.InvalidDataException;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class FormUrlEncodedParser {

    private FormUrlEncodedParser() {
    }

    public static Map<String, String> parse(Reader reader) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int read;

        while ((read = reader.read(buffer)) != -1) {
            body.append(buffer, 0, read);
        }

        if (body.length() == 0) {
            return Map.of();
        }

        Map<String, String> parameters = new HashMap<>();
        for (String pair : body.toString().split("&", -1)) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] keyAndValue = pair.split("=", 2);
            if (keyAndValue.length != 2) {
                throw new InvalidDataException("Malformed form body");
            }

            String key = decode(keyAndValue[0]);
            String value = decode(keyAndValue[1]);

            if (key.isBlank() || parameters.putIfAbsent(key, value) != null) {
                throw new InvalidDataException("Malformed form body");
            }
        }

        return Map.copyOf(parameters);
    }

    public static BigDecimal getRequiredDecimal(Map<String, String> parameters, String fieldName) {
        String value = parameters.get(fieldName);
        if (value == null || value.isBlank()) {
            throw new InvalidDataException("Missing form fields");
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Incorrect value in rate field");
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Malformed form body");
        }
    }
}
