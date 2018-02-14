package com.darts.mis;

import antlr.collections.impl.IntRange;
import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;
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
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by jpc on 31-05-17.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
    private static final int FIRST_YEAR = 2006;
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
        final Optional<Subscription> optionalSubscription = dataFacade.findSubscriptionByIds(Collections.singleton(id)).stream().findFirst();
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
        final Optional<Account> optionalAccount = dataFacade.findAccountByIds(Collections.singleton(id)).stream().findFirst();
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
                final XSSFWorkbook workbook = new XSSFWorkbook();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ){
            final LocalDate now = LocalDate.now();
            final List<Account> accounts = dataFacade.findAccountByIds(Arrays.asList(1L, 4L));
            final List<String> currencies = dataFacade.findAllCurrencies().stream().sorted().collect(Collectors.toList());
            LOGGER.debug("Currencies: {}", currencies);
            final Map<Long, Map<Domain, Long>> queryCounts = dataFacade.countSubscriptionQueriesByDomain();
            LOGGER.debug("Query counts: {}", queryCounts);
            final XSSFSheet summarySheet = workbook.createSheet("Summary");
            final XSSFSheet totalSheet = workbook.createSheet("Total");
            final Map<String, XSSFSheet> currencySheets = currencies.stream().collect(Collectors.toMap(Function.identity(), workbook::createSheet));
            final List<Integer> years = IntStream.range(FIRST_YEAR, now.getYear()).boxed().collect(Collectors.toList());
            // first structure output sheet
            final String[] currencySheetTitles = { "Id", "Name", "Country", "User count", "Nature" };
            int firstDataRow = 3;
            int firstDataCol = currencySheetTitles.length;
            currencySheets.values().forEach(sheet -> {
                final Row titleRow = sheet.createRow(firstDataRow - 1);
                int col = firstDataCol;
                for (final Integer year: years){
                    final Cell yearCell = titleRow.createCell(col);
                    yearCell.setCellValue(year.toString());
                    col++;
                }
                int row = firstDataRow;
                for (Account account: accounts){
                    col = firstDataCol - 1;
                    final Row accountRow = sheet.createRow(row);
                    final Cell accountNatureCell = accountRow.createCell(col--);
                    accountNatureCell.setCellValue(account.getStatus().toString());
                    final Cell userCountCell = accountRow.createCell(col--);
                    userCountCell.setCellValue(account.getUsers().size());
                    final Cell countryCell = accountRow.createCell(col--);
                    countryCell.setCellValue(account.getCountry());
                    final Cell nameCell = accountRow.createCell(col--);
                    nameCell.setCellValue(account.getName());
                    final Cell idCell = accountRow.createCell(col--);
                    idCell.setCellValue(account.getId());
                    row++;
                }
            });
            // fill data
            int rowNum = firstDataRow;
            for (final Account account: accounts) {
                final Schedule revenue = account.getRevenue();
                int colNum = firstDataCol;
                for (Integer year: years){
                    final LocalDate inc = LocalDate.of(year, 1, 1);
                    final LocalDate exc = inc.plusYears(1);
                    final Position position = revenue.accumulated(inc, exc);
                    for (String currency: currencies){
                        final XSSFSheet currencySheet = currencySheets.get(currency);
                        final Row row = currencySheet.getRow(rowNum);
                        final BigDecimal amount = position.getOrDefault(currency, BigDecimal.ZERO);
                        final Cell cellRevenue = row.createCell(colNum);
                        cellRevenue.setCellValue(amount.doubleValue());
                    }
                    colNum++;
                }
                rowNum++;
            }
            workbook.write(baos);
            return baos.toByteArray();

        } catch(IOException e){
            LOGGER.error("Cannot create excel workbook", e);
        }
        return null;
    }
}
