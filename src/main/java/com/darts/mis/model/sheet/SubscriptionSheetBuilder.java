package com.darts.mis.model.sheet;

import com.darts.mis.LocalDateRange;
import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.ForexModel;
import com.darts.mis.model.RevenueModel;
import com.darts.mis.model.SubscriptionItem;
import com.darts.mis.util.Conversions;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SubscriptionSheetBuilder implements SheetBuilder {
    private final static String[] TITLES = { "Account id", "Account name", "Subscription id", "Last from", "Last to", "Cancelled" };
    private final RevenueModel revenueModel;
    private final ForexModel forexModel;

    @Autowired
    public SubscriptionSheetBuilder(RevenueModel revenueModel, ForexModel forexModel) {
        this.revenueModel = revenueModel;
        this.forexModel = forexModel;
    }

    @Override
    public void addSheets(HSSFWorkbook workbook, int year) {
        final LocalDateRange range = LocalDateRange.of(year, 1, 1, year + 1, 1, 1);
        final CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        final Position rates = forexModel.getAverageRate(range).inverse(MathContext.DECIMAL64);
        final AtomicInteger row = new AtomicInteger();
        final Sheet sheet = workbook.createSheet("Subscriptions");

        sheet.createRow(row.getAndIncrement()).createCell(0).setCellValue("Subscription revenues in EUR for: " + range);
        final Row titleRow = sheet.createRow(row.getAndIncrement());
        final AtomicInteger titleCol = new AtomicInteger();
        Arrays.stream(TITLES).forEach(title -> titleRow.createCell(titleCol.getAndIncrement()).setCellValue(title));
        Arrays.stream(Domain.values()).forEach(domain -> {
//            titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s Active", domain));
            titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s Total", domain));
            titleRow.createCell(titleCol.getAndIncrement()).setCellValue(String.format("%s (EUR)", domain));
        });
        for (final AccountItem accountItem: revenueModel.getAccountItems()) {
            for (final SubscriptionItem subscriptionItem: accountItem.getSubscriptionItems()){
                subscriptionItem.getLastRenewOrUpdate().ifPresent(se -> {
                    if (range.isOverlapping(se.getRange())) {
                        final Row subscriptionRow = sheet.createRow(row.getAndIncrement());
                        final AtomicInteger subscriptionCol = new AtomicInteger();
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(accountItem.getAccount().getId());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(accountItem.getAccount().getName());
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionItem.getSubscription().getId());
                        Cell fromCell = subscriptionRow.createCell(subscriptionCol.getAndIncrement());
                        fromCell.setCellValue(Conversions.localDateToCalendar(se.getFrom()));
                        fromCell.setCellStyle(dateCellStyle);
                        Cell toCell = subscriptionRow.createCell(subscriptionCol.getAndIncrement());
                        toCell.setCellValue(Conversions.localDateToCalendar(se.getTo()));
                        toCell.setCellStyle(dateCellStyle);
                        subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionItem.isCancelled());
                        final Set<Domain> subscriptionDomains = subscriptionItem.getSubscription().getDomains();
                        final Map<Domain, Schedule> subscriptionRevenues = subscriptionItem.getRevenues();
                        Arrays.stream(Domain.values()).forEach(domain -> {
//                            subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(subscriptionDomains.contains(domain));
                            final Position accumulated = subscriptionRevenues.getOrDefault(domain, Schedule.EMPTY).accumulated(range);
                            subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(accumulated.toString());
                            final BigDecimal amountInEur = accumulated.dot(rates);
                            subscriptionRow.createCell(subscriptionCol.getAndIncrement()).setCellValue(amountInEur.doubleValue());
                        });
                    }
                });
            }
        }
    }
}
