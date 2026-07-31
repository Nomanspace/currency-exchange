package org.nomanspace.currencyexchange.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //можно напрямую десериализовать в writeValue тогда можно провести все внутри джексона. и возвращать ничего не надо
    //т.к. передав поток с записью, он сам его запишет по окончанию работы.
    public static void toJson(Writer writer, Object object) throws IOException {
        OBJECT_MAPPER.writeValue(writer, object);
    }


    public static <T> T fromJson(Reader reader, Class<T> clazz) throws IOException {
        T result = null;
        result = OBJECT_MAPPER.readValue(reader, clazz);
        return result;
    }

}
