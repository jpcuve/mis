package com.darts.mis.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public Date convertToDatabaseColumn(LocalDate localDate) {
        return localDate == null ? null : new Date(localDate.atStartOfDay(ZONE_ID).toInstant().toEpochMilli());
    }

    @Override
    public LocalDate convertToEntityAttribute(Date date) {
        return date == null ? null : Instant.ofEpochMilli(date.getTime()).atZone(ZONE_ID).toLocalDate();
    }
}
