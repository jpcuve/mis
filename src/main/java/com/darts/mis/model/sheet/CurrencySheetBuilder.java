package com.darts.mis.model.sheet;

import com.darts.mis.LocalDateRange;
import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.ForexModel;
import com.darts.mis.model.RevenueModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CurrencySheetBuilder implements SheetBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencySheetBuilder.class);
    private final String[] titles = { "Id", "Name" };
    private final RevenueModel revenueModel;
    private final ForexModel forexModel;

    @Autowired
    public CurrencySheetBuilder(RevenueModel revenueModel, ForexModel forexModel) {
        this.revenueModel = revenueModel;
        this.forexModel = forexModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook, int year) {
        final LocalDateRange range = LocalDateRange.of(year, 1, 1, year + 1, 1, 1);
        final Position rates = forexModel.getAverageRate(range);
        revenueModel.getCurrencies().forEach(iso -> {
            final Sheet sheet = workbook.createSheet(String.format("%s", iso));
            final BigDecimal rate = rates.getOrDefault(iso, BigDecimal.ZERO);
            final AtomicInteger row = new AtomicInteger();
            final Row infoRow = sheet.createRow(row.getAndIncrement());
            final AtomicInteger infoCol = new AtomicInteger();
            infoRow.createCell(infoCol.getAndIncrement()).setCellValue("Revenues in " + iso + " for: " + range + "; average exchange rate: " + rate);
            final Row titleRow = sheet.createRow(row.getAndIncrement());
            final AtomicInteger titleCol = new AtomicInteger();
            Arrays.stream(titles).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
            for (final Domain domain : Domain.values()) {
                titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s (%s)", domain, iso));
                titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s (EUR)", domain));
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
                    final BigDecimal amountInIso = position.getOrDefault(iso, BigDecimal.ZERO);
                    accountRow.createCell(accountCol.getAndIncrement()).setCellValue(amountInIso.doubleValue());
                    final BigDecimal amountInEur = amountInIso.divide(rate, MathContext.DECIMAL64);
                    accountRow.createCell(accountCol.getAndIncrement()).setCellValue(amountInEur.doubleValue());
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
