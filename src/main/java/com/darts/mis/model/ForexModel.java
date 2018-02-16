package com.darts.mis.model;

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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.TreeMap;

@Component
@Scope("singleton")
public class ForexModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForexModel.class);
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private TreeMap<LocalDate, Position> rates = new TreeMap<>();

    public static class Quote {
        private final String currency;
        private final LocalDate from;
        private final BigDecimal rate;
        private final BigDecimal inverseRate;

        @JsonCreator
        public Quote(
                @JsonProperty("currency") String currency,
                @JsonDeserialize(using = LocalDateDeserializer.class) @JsonProperty("from") LocalDate from,
                @JsonProperty("rate") BigDecimal rate,
                @JsonProperty("inverseRate") BigDecimal inverseRate) {
            this.currency = currency;
            this.from = from;
            this.rate = rate;
            this.inverseRate = inverseRate;
        }
    }

    @PostConstruct
    public void init(){
        try{
            final Quote[] quotes = mapper.readValue(getClass().getClassLoader().getSystemResourceAsStream("rates.yaml"), Quote[].class);
            Arrays.stream(quotes).forEach(quote ->
                rates.computeIfAbsent(quote.from, ld -> new Position()).putAmount(quote.currency, quote.inverseRate)
            );
        } catch(IOException e){
            LOGGER.error("Cannot read rate resource", e);
        }
    }

    public void hello(){
    }

}
