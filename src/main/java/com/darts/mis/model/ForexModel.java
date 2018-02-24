package com.darts.mis.model;

import com.darts.mis.LocalDateRange;
import com.darts.mis.Position;
import com.darts.mis.util.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Scope("singleton")
public class ForexModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForexModel.class);
    private static final MathContext MATH_CONTEXT = new MathContext(6, RoundingMode.FLOOR);
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private TreeMap<LocalDate, Position> rates = new TreeMap<>();

    public static class Quote {
        private final String currency;
        private final LocalDate from;
        private final BigDecimal rate;

        @JsonCreator
        public Quote(
                @JsonProperty("currency") String currency,
                @JsonDeserialize(using = LocalDateDeserializer.class) @JsonProperty("from") LocalDate from,
                @JsonProperty("rate") BigDecimal rate) {
            this.currency = currency;
            this.from = from;
            this.rate = rate;
        }
    }

    @PostConstruct
    public void init() {
        try {
            final Quote[] quotes = mapper.readValue(getClass().getClassLoader().getSystemResourceAsStream("rates.yaml"), Quote[].class);
            Arrays.stream(quotes).forEach(quote ->
                    rates.computeIfAbsent(quote.from, ld -> Position.of("EUR", 1)).putAmount(quote.currency, quote.rate)
            );
            final List<LocalDate> localDates = new ArrayList<>(rates.keySet());
            if (localDates.isEmpty()) {
                throw new IllegalStateException("No rates");
            }
            rates.put(LocalDate.MIN, rates.get(localDates.get(0)));
            rates.put(LocalDate.MAX, rates.get(localDates.get(localDates.size() - 1)));
        } catch (IOException e) {
            LOGGER.error("Cannot read rate resource", e);
        }
    }

    public Position getRate(LocalDate localDate) {
        Position p = null;
        for (final LocalDate ld : rates.keySet()) {
            if (p != null && ld.isAfter(localDate)) {
                break;
            }
            p = rates.get(ld);
        }
        return p;
    }

    public Position getAverageRate(LocalDateRange range) {
        Map.Entry<LocalDate, Position> lastEntry = null;
        BigDecimal totalDays = BigDecimal.ZERO;
        Position position = Position.ZERO;
        for (final Map.Entry<LocalDate, Position> entry : rates.entrySet()) {
            if (lastEntry != null) {
                LocalDate from = range.getFrom().isAfter(lastEntry.getKey()) ? range.getFrom() : lastEntry.getKey();
                LocalDate to = range.getTo().isAfter(entry.getKey()) ? entry.getKey() : range.getTo();
//                LOGGER.debug("From: {}, to: {}", from, to);
                if (to.isAfter(from)) {
                    BigDecimal interval = new BigDecimal(ChronoUnit.DAYS.between(from, to));
//                    LOGGER.debug("Using day count: {}", interval);
                    totalDays = totalDays.add(interval);
                    position = position.add(lastEntry.getValue().scalar(interval));
                }
            }
            lastEntry = entry;
        }
        position = position.inverseScalar(totalDays, MATH_CONTEXT);
        return position;
    }
}
