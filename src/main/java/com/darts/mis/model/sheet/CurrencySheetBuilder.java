package com.darts.mis.model.sheet;

import com.darts.mis.LocalDateRange;
import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.RevenueModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CurrencySheetBuilder implements SheetBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencySheetBuilder.class);
    private final String[] titles = { "Id", "Name" };
    private final RevenueModel revenueModel;

    @Autowired
    public CurrencySheetBuilder(RevenueModel revenueModel) {
        this.revenueModel = revenueModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook, int year) {
        final LocalDateRange range = LocalDateRange.of(year, 1, 1, year + 1, 1, 1);
        revenueModel.getCurrencies().forEach(iso -> {
            final Sheet sheet = workbook.createSheet(String.format("(%s)", iso));
            final AtomicInteger row = new AtomicInteger();
            final Row titleRow = sheet.createRow(row.getAndIncrement());
            final AtomicInteger titleCol = new AtomicInteger();
            Arrays.stream(titles).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
            for (final Domain domain : Domain.values()) {
                titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s %s", year, domain));
            }
            for (final AccountItem accountItem: revenueModel.getAccountItems()) {
                LOGGER.debug("Account: {} {}", accountItem.getAccount().getId(), accountItem.getAccount().getName());
                final Row accountRow = sheet.createRow(row.getAndIncrement());
                final AtomicInteger accountCol = new AtomicInteger();
                accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
                accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
                final Map<Domain, Schedule> revenues = accountItem.getRevenues();
                for (final Domain domain: Domain.values()){
                    final Position position = revenues.getOrDefault(domain, Schedule.EMPTY).accumulated(range);
                    accountRow.createCell(accountCol.getAndIncrement()).setCellValue(position.getOrDefault(iso, BigDecimal.ZERO).doubleValue());
                }
            }
        });

        /*
        sheets.forEach((currency, sheet) -> {
            final Map<Integer, Position> yearlyExchangeRates = years
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            year -> forexModel.getAverageRate(LocalDateRange.of(year, 1, 1, year + 1, 1, 1)))
                    );
            final Row titleRow = sheet.createRow(FIRST_DATA_ROW - 1);
            // TODO average rate over year
            final Row exchangeRateRow = sheet.createRow(FIRST_DATA_ROW - 2);
            exchangeRateRow.createCell(0).setCellValue("Exchange rates");
            int col = CURRENCY_SHEET_TITLES.length;
            for (final Integer year: years){
                for (final Domain domain: Domain.values()){
                    final Cell exchangeRateCell = exchangeRateRow.createCell(col);
                    final Position exchangeRate = yearlyExchangeRates.getOrDefault(year, Position.ZERO);
                    if (!exchangeRate.containsKey(currency)){
                        throw new IllegalStateException("Exchange rate missing for currency: " + currency);
                    }
                    exchangeRateCell.setCellValue(exchangeRate.get(currency).doubleValue());
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
                final LocalDateRange range = LocalDateRange.of(year, 1, 1, year + 1, 1, 1);
                for (final Domain domain: Domain.values()){
                    final Position position = revenues.getOrDefault(domain, Schedule.EMPTY).accumulated(range);
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
*/
    }
}
