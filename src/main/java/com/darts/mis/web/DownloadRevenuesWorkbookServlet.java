package com.darts.mis.web;

import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.RevenueModel;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = { "/download-revenues" })
public class DownloadRevenuesWorkbookServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadRevenuesWorkbookServlet.class);
    private final RevenueModel revenueModel;

    @Autowired
    public DownloadRevenuesWorkbookServlet(RevenueModel revenueModel){
        this.revenueModel = revenueModel;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try(final HSSFWorkbook workbook = new HSSFWorkbook()){
            final int maxYear = LocalDate.now().getYear();
            final List<String> currencies = revenueModel.getCurrencies()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
            LOGGER.debug("Currencies: {}", currencies);
            final List<Integer> years = revenueModel.getYears()
                    .stream()
                    .filter(y -> y <= maxYear)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            LOGGER.debug("Years: {}", years);
            final HSSFSheet summarySheet = workbook.createSheet("Summary");
            summarySheet.createRow(0).createCell(0).setCellValue("Summary");
            final HSSFSheet totalSheet = workbook.createSheet("Total");
            totalSheet.createRow(0).createCell(0).setCellValue("Total");
            final Map<String, HSSFSheet> currencySheets = currencies
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), workbook::createSheet));
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
                for (final AccountItem accountItem: revenueModel.getAccountItems()){
                    final Row accountRow = sheet.createRow(row);
                    final Cell idCell = accountRow.createCell(0);
                    idCell.setCellValue(accountItem.getAccount().getId());
                    final Cell nameCell = accountRow.createCell(1);
                    nameCell.setCellValue(accountItem.getAccount().getName());
                    final Cell countryCell = accountRow.createCell(2);
                    countryCell.setCellValue(accountItem.getAccount().getCountry());
                    final Cell userCountCell = accountRow.createCell(3);
                    userCountCell.setCellValue(accountItem.getAccount().getUsers().size());
                    final Cell accountNatureCell = accountRow.createCell(4);
                    accountNatureCell.setCellValue(accountItem.getAccount().getStatus().toString());
                    row++;
                }
            });
            // fill data
            int rowNum = firstDataRow;
            for (final AccountItem accountItem: revenueModel.getAccountItems()) {
                LOGGER.debug("Account: {} {}", accountItem.getAccount().getId(), accountItem.getAccount().getName());
                final Map<Domain, Schedule> revenues = accountItem.getRevenues();
                int colNum = currencySheetTitles.length;
                for (Integer year: years){
                    final LocalDate inc = LocalDate.of(year, 1, 1);
                    final LocalDate exc = inc.plusYears(1);
                    for (final Domain domain: Domain.values()){
                        final Position position = revenues.getOrDefault(domain, Schedule.EMPTY).accumulated(inc, exc);
                        for (String currency: currencies){
                            final HSSFSheet currencySheet = currencySheets.get(currency);
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
            final String filename = String.format("Revenues_%s.xls", LocalDateTime.now().toString().replace('.', '_').replace('-', '_'));
            res.setHeader("Content-Disposition", "attachment; filename=" + filename);
            workbook.write(res.getOutputStream());
        }
    }
}
