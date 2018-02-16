package com.darts.mis.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LocalDateDeserializer() {
        super(LocalDate.class);
    }

    public LocalDateDeserializer(JavaType valueType) {
        super(valueType);
    }

    public LocalDateDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final String text = jsonParser.getText();
        return LocalDate.parse(text, FORMATTER);
    }
}
