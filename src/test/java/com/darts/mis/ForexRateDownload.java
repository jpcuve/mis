package com.darts.mis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Scanner;

public class ForexRateDownload {
    public static final String[] CURRENCIES = { "brl", "cny", "gbp", "inr", "jpy", "nzd", "twd", "usd" };

    public static void main(String[] args) throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final ArrayNode array = mapper.createArrayNode();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            Arrays.stream(CURRENCIES).forEach(currency -> {
                final String url = String.format("http://sdw.ecb.europa.eu/quickviewexport.do?SERIES_KEY=120.EXR.H.%s.EUR.SP00.A&type=csv", currency.toUpperCase());
                final HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse res = httpClient.execute(httpGet)) {
                    if (res.getStatusLine().getStatusCode() != 200) {
                        throw new IOException("Invalid http response: " + res.getStatusLine());
                    }
                    final Scanner scanner = new Scanner(res.getEntity().getContent());
                    for (int i = 0; i < 5; i++){
                        scanner.nextLine();
                    }
                    while(scanner.hasNext()){
                        final String line = scanner.nextLine();
                        int comma = line.indexOf(',');
                        if (comma > 0){
                            final String period = line.substring(0, comma);
                            final BigDecimal rate = new BigDecimal(line.substring(comma + 1));
                            final LocalDate from = LocalDate.of(
                                    Integer.parseInt(period.substring(0, 4)),
                                    period.endsWith("H1") ? 1 : 7,
                                    1
                            );
                            final ObjectNode node = array.addObject();
                            node.put("currency", currency.toUpperCase());
                            node.put("from", from.toString());
                            node.put("rate", rate);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File("etc/rates.yaml"))){
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileOutputStream, array);
        }
    }
}
