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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AccountSheetBuilder implements SheetBuilder {
    private final String[] titles = { "Id", "Name", "Country", "User count", "Status" };
    private final RevenueModel revenueModel;
    private final ForexModel forexModel;

    @Autowired
    public AccountSheetBuilder(RevenueModel revenueModel, ForexModel forexModel) {
        this.revenueModel = revenueModel;
        this.forexModel = forexModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook, int year) {
        final LocalDateRange range = LocalDateRange.of(year, 1, 1, year + 1, 1, 1);
        final Position rates = forexModel.getAverageRate(range).inverse(MathContext.DECIMAL64);
        final Sheet sheet = workbook.createSheet("Accounts");
        final AtomicInteger row = new AtomicInteger();
        sheet.createRow(row.getAndIncrement()).createCell(0).setCellValue("Total revenues in EUR for: " + range);
        final Row titleRow = sheet.createRow(row.getAndIncrement());
        final AtomicInteger titleCol = new AtomicInteger();
        Arrays.stream(titles).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
        Arrays.stream(Domain.values()).forEach(domain -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(domain.toString()));
        titleRow.createCell(titleCol.getAndIncrement()).setCellValue("Total");
        for (final AccountItem accountItem: revenueModel.getAccountItems()) {
            final Row accountRow = sheet.createRow(row.getAndIncrement());
            final AtomicInteger accountCol = new AtomicInteger();
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getCountry());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getUsers().size());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getStatus().toString());
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            BigDecimal totalInEur = BigDecimal.ZERO;
            for (Domain domain: Domain.values()) {
                final Schedule domainRevenues = revenues.getOrDefault(domain, Schedule.EMPTY);
                final Position accumulated = domainRevenues.accumulated(range);
                final BigDecimal amountInEur = accumulated.dot(rates);
                accountRow.createCell(accountCol.getAndIncrement()).setCellValue(amountInEur.doubleValue());
                totalInEur = totalInEur.add(amountInEur);
            }
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(totalInEur.doubleValue());
        }

    }
}
