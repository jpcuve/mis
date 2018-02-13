package com.darts.mis;

import com.darts.mis.domain.Account;
import com.darts.mis.domain.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jpc on 31-05-17.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
    private final DataFacade dataFacade;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ApiController(DataFacade dataFacade){
        this.dataFacade = dataFacade;
    }

    @GetMapping("/check-subscriptions")
    public String checkSubscriptions(){
        for (final Subscription subscription: dataFacade.findAllSubscriptions()){
            LOGGER.debug("Checking subscription: " + subscription.getId());
            subscription.getRevenue();
        }
        return "OK";
    }

    @GetMapping("/subscription-revenues/{id}")
    public ObjectNode subscriptionRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<Subscription> optionalSubscription = dataFacade.findSubscriptionById(id);
        if (optionalSubscription.isPresent()) {
            Schedule revenue = optionalSubscription.get().getRevenue();
            ret.putPOJO("revenue", revenue);
            ret.putPOJO("total", revenue.accumulatedTo(now));
        }
        return ret;
    }

    @GetMapping("/account-revenues/{id}")
    public ObjectNode accountRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<Account> optionalAccount = dataFacade.findAccountById(id);
        if (optionalAccount.isPresent()) {
            Schedule revenue = optionalAccount.get().getRevenue();
            ret.putPOJO("revenue", revenue);
            ret.putPOJO("total", revenue.accumulatedTo(now));
        }
        return ret;
    }

    @GetMapping("/revenues")
    public ArrayNode revenues(){
        final LocalDate now = LocalDate.now();
        final ArrayNode ret = mapper.createArrayNode();
        for (final Account account: dataFacade.findAllAccounts()){
            final Schedule revenue = account.getRevenue();
            final Position position = revenue.accumulatedTo(now);
            final ObjectNode node = ret.addObject();
            System.out.println(account.getId());
            node.put("id", account.getId());
            node.put("name", account.getName());
            node.put("revenue", position.toString());
        }
        return ret;
    }

    @GetMapping(value = "/data.xlsx", produces = "application/vnd.ms-excel")
    public byte[] xlsTest(){
        try(
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ){
            final LocalDate now = LocalDate.now();
            final List<String> currencies = dataFacade.findAllCurrencies().stream().sorted().collect(Collectors.toList());
            final XSSFSheet sheet = workbook.createSheet("Total revenues");
            int rowNum = 0;
            for (final Account account: dataFacade.findAllAccounts()) {
                final Schedule revenue = account.getRevenue();
                final Position position = revenue.accumulatedTo(now);
                final Row row = sheet.createRow(rowNum++);
                final Cell cellName = row.createCell(0);
                cellName.setCellValue(account.getName());
                final Cell cellRevenue = row.createCell(1);
                cellRevenue.setCellValue(position.toString());
            }
            workbook.write(baos);
            return baos.toByteArray();

        } catch(IOException e){
            LOGGER.error("Cannot create excel workbook", e);
        }
        return null;
    }
}
