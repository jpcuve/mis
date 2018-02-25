package com.darts.mis.model.sheet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public interface SheetBuilder {
    void addSheets(HSSFWorkbook workbook);
}
