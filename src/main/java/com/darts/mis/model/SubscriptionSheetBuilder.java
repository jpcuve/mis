package com.darts.mis.model;

import com.darts.mis.domain.Domain;
import com.darts.mis.domain.SubscriptionEdit;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SubscriptionSheetBuilder implements SheetBuilder {
    private final String[] titles = { "Account id", "Account name", "Subscription id", "Last from", "Last to", "Cancelled" };
    private final RevenueModel revenueModel;

    @Autowired
    public SubscriptionSheetBuilder(RevenueModel revenueModel) {
        this.revenueModel = revenueModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook) {
        int year = Year.now().getValue();
        final LocalDate from = LocalDate.of(year - 1, 1, 1);
        final LocalDate to = LocalDate.of(year, 1, 1);
        final AtomicInteger row = new AtomicInteger();
        final Sheet sheet = workbook.createSheet("Subscriptions");

        sheet.createRow(row.getAndIncrement()).createCell(0).setCellValue("Subscription revenues from: " + from + " to: " + to);
        final Row titleRow = sheet.createRow(row.getAndIncrement());
        final AtomicInteger titleCol = new AtomicInteger();
        Arrays.stream(titles).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
        Arrays.stream(Domain.values()).forEach(domain -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(domain.toString()));
        for (final AccountItem accountItem: revenueModel.getAccountItems()) {
            for (final SubscriptionItem subscriptionItem: accountItem.getSubscriptionItems()){
                subscriptionItem.getLastRenewOrUpdate().ifPresent(se -> {
                    if (!(se.getTo().isBefore(from) || se.getFrom().isAfter(to))){ // TODO change this test to any subscription exists between from and to
                        final Row subscriptionRow = sheet.createRow(row.getAndIncrement());
                        final AtomicInteger subscriptionCol = new AtomicInteger();
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionItem.getSubscription().getId());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(se.getFrom().toString());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(se.getTo().toString());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionItem.isCancelled());
                        final Set<Domain> subscriptionDomains = subscriptionItem.getSubscription().getDomains();
                        Arrays.stream(Domain.values()).forEach(domain -> subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionDomains.contains(domain)));
                    }
                });
            }
        }


    }
}
