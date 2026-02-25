package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWrapper extends DownloadableFile {
    private XSSFWorkbook workbook;

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }
}
