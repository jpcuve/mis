package com.darts.mis;

import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;
import com.darts.mis.domain.Subscription;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.RevenueModel;
import com.darts.mis.model.SubscriptionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final RevenueModel revenueModel;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ApiController(DataFacade dataFacade){
        this.dataFacade = dataFacade;
        this.revenueModel = new RevenueModel(dataFacade);
    }

    @GetMapping("/check-subscriptions")
    public String checkSubscriptions(){
        for (final Subscription subscription: dataFacade.findAllSubscriptions()){
            LOGGER.debug("Checking subscription: " + subscription.getId());
            final SubscriptionItem subscriptionItem = new SubscriptionItem(subscription, revenueModel.getQueryCounts().getOrDefault(subscription.getId(), Collections.emptyMap()));
        }
        return "OK";
    }

    @GetMapping("/subscription-revenues/{id}")
    public ObjectNode subscriptionRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<SubscriptionItem> optionalSubscriptionItem = revenueModel.findSubscription(id);
        if (optionalSubscriptionItem.isPresent()) {
            final SubscriptionItem subscriptionItem = optionalSubscriptionItem.get();
            final Map<Domain, Schedule> revenues = subscriptionItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        }
        return ret;
    }

    @GetMapping("/account-revenues/{id}")
    public ObjectNode accountRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<AccountItem> optionalAccountItem = revenueModel.findAccount(id);
        if (optionalAccountItem.isPresent()) {
            final AccountItem accountItem = optionalAccountItem.get();
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        }
        return ret;
    }

/*
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
            currencySheets.values().forEach(sheet -> {
                final Row titleRow = sheet.createRow(firstDataRow - 1);
                int col;
                for (col = 0; col < currencySheetTitles.length; col++){
                    final Cell titleCell = titleRow.createCell(col);
                    titleCell.setCellValue(currencySheetTitles[col]);
                }
                for (final Integer year: years){
                    for (final Domain domain: Domain.values()){
                        final Cell yearCell = titleRow.createCell(col);
                        yearCell.setCellValue(String.format("%s %s", year, domain));
                        col++;
                    }
                }
                int row = firstDataRow;
                for (Account account: accounts){
                    final Row accountRow = sheet.createRow(row);
                    final Cell idCell = accountRow.createCell(0);
                    idCell.setCellValue(account.getId());
                    final Cell nameCell = accountRow.createCell(1);
                    nameCell.setCellValue(account.getName());
                    final Cell countryCell = accountRow.createCell(2);
                    countryCell.setCellValue(account.getCountry());
                    final Cell userCountCell = accountRow.createCell(3);
                    userCountCell.setCellValue(account.getUsers().size());
                    final Cell accountNatureCell = accountRow.createCell(4);
                    accountNatureCell.setCellValue(account.getStatus().toString());
                    row++;
                }
            });
            // fill data
            int rowNum = firstDataRow;
            for (final Account account: accounts) {
                final Schedule revenue = account.getRevenue();
                int colNum = currencySheetTitles.length;
                for (Integer year: years){
                    final LocalDate inc = LocalDate.of(year, 1, 1);
                    final LocalDate exc = inc.plusYears(1);
                    final Position position = revenue.accumulated(inc, exc);
                    for (final Domain domain: Domain.values()){
                        for (String currency: currencies){
                            final XSSFSheet currencySheet = currencySheets.get(currency);
                            final Row row = currencySheet.getRow(rowNum);
                            final BigDecimal amount = position.getOrDefault(currency, BigDecimal.ZERO);
                            final Cell cellRevenue = row.createCell(colNum);
                            cellRevenue.setCellValue(amount.doubleValue());
                        }
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
*/
}
