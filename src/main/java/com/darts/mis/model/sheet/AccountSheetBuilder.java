package com.darts.mis.model.sheet;

import com.darts.mis.Schedule;
import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.RevenueModel;
import com.darts.mis.model.sheet.SheetBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AccountSheetBuilder implements SheetBuilder {
    private final String[] titles = { "Id", "Name", "Country", "User count", "Status" };
    private final RevenueModel revenueModel;

    @Autowired
    public AccountSheetBuilder(RevenueModel revenueModel) {
        this.revenueModel = revenueModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook, int year) {
        final LocalDate now = LocalDate.of(Year.now().getValue(), 1, 1);
        final Sheet sheet = workbook.createSheet("Accounts");
        final AtomicInteger row = new AtomicInteger();
        sheet.createRow(row.getAndIncrement()).createCell(0).setCellValue("Total revenues ever, accumulated to: " + now);
        final Row titleRow = sheet.createRow(row.getAndIncrement());
        final AtomicInteger titleCol = new AtomicInteger();
        Arrays.stream(titles).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
        Arrays.stream(Domain.values()).forEach(domain -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(domain.toString()));
        for (final AccountItem accountItem: revenueModel.getAccountItems()) {
            final Row accountRow = sheet.createRow(row.getAndIncrement());
            final AtomicInteger accountCol = new AtomicInteger();
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getCountry());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getUsers().size());
            accountRow.createCell(accountCol.getAndIncrement()).setCellValue(accountItem.getAccount().getStatus().toString());
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            Arrays.stream(Domain.values()).forEach(domain -> accountRow.createCell(accountCol.getAndIncrement()).setCellValue(revenues.getOrDefault(domain, Schedule.EMPTY).accumulatedTo(now).toString()));
        }

    }
}
