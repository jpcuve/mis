package com.darts.mis.web;

import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.AccountStatus;
import com.darts.mis.domain.Domain;
import com.darts.mis.domain.SubscriptionEdit;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.ForexModel;
import com.darts.mis.model.RevenueModel;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = { "/download-revenues" })
public class DownloadRevenuesWorkbookServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadRevenuesWorkbookServlet.class);
    private static final String[] CURRENCY_SHEET_TITLES = { "Id", "Name", "Country", "User count", "Status", "Last start", "Last end" };
    public static final int FIRST_DATA_ROW = 3;
    private final RevenueModel revenueModel;
    private final ForexModel forexModel;
    private List<String> currencies;
    private List<Integer> years;
    private List<AccountItem> accountItems;

    @Autowired
    public DownloadRevenuesWorkbookServlet(RevenueModel revenueModel, ForexModel forexModel){
        this.revenueModel = revenueModel;
        this.forexModel = forexModel;
    }

    @PostConstruct
    public void init(){
        this.currencies = revenueModel.getCurrencies()
                .stream()
                .sorted()
                .collect(Collectors.toList());
        LOGGER.debug("Currencies: {}", currencies);
        final int maxYear = LocalDate.now().getYear();
        this.years = revenueModel.getYears()
                .stream()
                .filter(y -> y <= maxYear)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        LOGGER.debug("Years: {}", years);
        this.accountItems = revenueModel.getAccountItems()
                .stream()
                .filter(a -> a.getAccount().getStatus() != AccountStatus.P)
                .collect(Collectors.toList());
        LOGGER.debug("Account item count: {}", accountItems.size());
    }

    private void fillSummarySheet(final HSSFSheet sheet){
        sheet.createRow(0).createCell(0).setCellValue("Summary");
        final AtomicInteger ai = new AtomicInteger(2);
        sheet.createRow(ai.getAndIncrement()).createCell(0).setCellValue("Exchange rates against EUR come from the ECB");
        sheet.createRow(ai.getAndIncrement()).createCell(0).setCellValue("Revenues are computed based on subscriptions and services, and adjusted against invoices");
        sheet.createRow(ai.getAndIncrement()).createCell(0).setCellValue("Revenues are split over domains based on the number of queries in the subscription");
        sheet.createRow(ai.getAndIncrement()).createCell(0).setCellValue("If number of queries is not available, revenues are split evenly over the domains of the subscription");
    }


    private int fillAccountTitle(Row row, int startColumn){
        final AtomicInteger ai = new AtomicInteger(startColumn);
        Arrays.stream(CURRENCY_SHEET_TITLES).forEach(s -> row.createCell(ai.getAndIncrement()).setCellValue(s));
        return ai.get();
    }

    private int fillAccountData(Row row, int startColumn, AccountItem accountItem){
        final AtomicInteger ai = new AtomicInteger(startColumn);
        row.createCell(ai.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
        row.createCell(ai.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
        row.createCell(ai.getAndIncrement()).setCellValue(accountItem.getAccount().getCountry());
        row.createCell(ai.getAndIncrement()).setCellValue(accountItem.getAccount().getUsers().size());
        row.createCell(ai.getAndIncrement()).setCellValue(accountItem.getAccount().getStatus().toString());
        final Optional<SubscriptionEdit> optionalSubscriptionEdit = accountItem.getLastRenewOrUpdate();
        row.createCell(ai.getAndIncrement()).setCellValue(optionalSubscriptionEdit.map(se -> se.getFrom().toString()).orElse("-"));
        row.createCell(ai.getAndIncrement()).setCellValue(optionalSubscriptionEdit.map(se -> se.getTo().toString()).orElse("-"));
        return ai.get();
    }

    private void fillTotalSheet(final HSSFSheet sheet){
        final LocalDate now = LocalDate.now();
        sheet.createRow(0).createCell(0).setCellValue("Total @ " + now);
        final Row titleRow = sheet.createRow(FIRST_DATA_ROW - 1);
        int col = fillAccountTitle(titleRow, 0);
        for (final Domain domain: Domain.values()){
            titleRow.createCell(col).setCellValue(domain.toString());
            col++;
        }
        int rowNum = FIRST_DATA_ROW;
        for (final AccountItem accountItem: accountItems) {
            final Row accountRow = sheet.createRow(rowNum);
            col = fillAccountData(accountRow, 0, accountItem);
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            for (final Domain domain: Domain.values()){
                accountRow.createCell(col).setCellValue(revenues.getOrDefault(domain, Schedule.EMPTY).accumulatedTo(now).toString());
                col++;
            }
            rowNum++;
        }
    }

    private void fillCurrencySheets(final Map<String, HSSFSheet> sheets){
        sheets.forEach((currency, sheet) -> {
            final Map<Integer, Position> yearlyExchangeRates = years
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), year -> forexModel.getRate(LocalDate.of(year, 1, 1))));
            final Row titleRow = sheet.createRow(FIRST_DATA_ROW - 1);
            // TODO average rate over year
            final Row exchangeRateRow = sheet.createRow(FIRST_DATA_ROW - 2);
            exchangeRateRow.createCell(0).setCellValue("Exchange rates");
            int col = CURRENCY_SHEET_TITLES.length;
            for (final Integer year: years){
                for (final Domain domain: Domain.values()){
                    final Cell exchangeRateCell = exchangeRateRow.createCell(col);
                    exchangeRateCell.setCellValue(yearlyExchangeRates.getOrDefault(year, Position.ZERO).get(currency).doubleValue());
                    col++;
                }
            }
            col = fillAccountTitle(titleRow, 0);
            for (final Integer year: years){
                for (final Domain domain: Domain.values()){
                    final Cell yearCell = titleRow.createCell(col);
                    yearCell.setCellValue(String.format("%s %s", year, domain));
                    col++;
                }
            }
            int row = FIRST_DATA_ROW;
            for (final AccountItem accountItem: accountItems){
                fillAccountData(sheet.createRow(row), 0, accountItem);
                row++;
            }
        });
        // fill data
        int rowNum = FIRST_DATA_ROW;
        for (final AccountItem accountItem: accountItems) {
            LOGGER.debug("Account: {} {}", accountItem.getAccount().getId(), accountItem.getAccount().getName());
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            int colNum = CURRENCY_SHEET_TITLES.length;
            for (Integer year: years){
                final LocalDate inc = LocalDate.of(year, 1, 1);
                final LocalDate exc = inc.plusYears(1);
                for (final Domain domain: Domain.values()){
                    final Position position = revenues.getOrDefault(domain, Schedule.EMPTY).accumulated(inc, exc);
                    for (String currency: currencies){
                        final HSSFSheet currencySheet = sheets.get(currency);
                        final Row row = currencySheet.getRow(rowNum);
                        final BigDecimal amount = position.getOrDefault(currency, BigDecimal.ZERO);
                        final Cell cellRevenue = row.createCell(colNum);
                        cellRevenue.setCellValue(amount.doubleValue());
                    }
                    colNum++;
                }
            }
            rowNum++;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try(final HSSFWorkbook workbook = new HSSFWorkbook()){
            fillSummarySheet(workbook.createSheet("Summary"));
            fillTotalSheet(workbook.createSheet("Total"));
            fillCurrencySheets(currencies
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), workbook::createSheet))
            );
            final String filename = String.format("Revenues_%s.xls", LocalDateTime.now().toString().replace('.', '_').replace('-', '_'));
            res.setHeader("Content-Disposition", "attachment; filename=" + filename);
            workbook.write(res.getOutputStream());
        }
    }
}
