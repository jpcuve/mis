package com.darts.mis.web;

import com.darts.mis.model.sheet.AccountSheetBuilder;
import com.darts.mis.model.sheet.CurrencySheetBuilder;
import com.darts.mis.model.sheet.SubscriptionSheetBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;

@WebServlet(urlPatterns = { "/download-revenues" })
public class DownloadRevenuesWorkbookServlet extends HttpServlet {
    private final AccountSheetBuilder accountSheetBuilder;
    private final SubscriptionSheetBuilder subscriptionSheetBuilder;
    private final CurrencySheetBuilder currencySheetBuilder;

    @Autowired
    public DownloadRevenuesWorkbookServlet(
            AccountSheetBuilder accountSheetBuilder,
            SubscriptionSheetBuilder subscriptionSheetBuilder,
            CurrencySheetBuilder currencySheetBuilder
    ){
        this.accountSheetBuilder = accountSheetBuilder;
        this.subscriptionSheetBuilder = subscriptionSheetBuilder;
        this.currencySheetBuilder = currencySheetBuilder;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        final String yearAsString = req.getParameter("year");
        int year = Year.now().getValue() - 1;
        if (yearAsString != null)  try{
            year = Integer.parseInt(yearAsString);
        } catch (NumberFormatException e){
            // ignore
        }
        try(final HSSFWorkbook workbook = new HSSFWorkbook()){
            accountSheetBuilder.addSheets(workbook, year);
            subscriptionSheetBuilder.addSheets(workbook, year);
            currencySheetBuilder.addSheets(workbook, year);
            final String filename = String.format("Revenues_year_%s_made_%s.xls", year, LocalDateTime.now().toString().replace('.', '_').replace('-', '_'));
            res.setHeader("Content-Disposition", "attachment; filename=" + filename);
            workbook.write(res.getOutputStream());
        }
    }
}
