package uk.co.epicuri.serverapi.service.external;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PaymentSenseReportingUtil {
    public static XSSFWorkbook toWorkBook(List<PACReport> reports) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        int i = 1;
        for(PACReport report : reports) {
            if(report.getReportTime() == null) {
                continue;
            }
            addSheet(workbook, report, i++);
        }

        return workbook;
    }

    private static XSSFSheet addSheet(XSSFWorkbook workbook, PACReport report, int sheetNum) {
        XSSFSheet sheet = workbook.createSheet(report.getReportType() + " " + sheetNum);

        createBalanceRows(sheet, report.getBalances());
        int rowNum = createTotalsRows(4, "Issuer Totals", sheet, report.getBalances().getIssuerTotals());
        rowNum = createTotalsRows(rowNum + 1, "Waiter Totals", sheet, report.getBalances().getWaiterTotals());

        Row bankingRow = sheet.createRow(rowNum++);
        createAndSetStringCell(bankingRow, rowNum++, "BANKING");

        for(Map.Entry<String,Acquirer> entry : report.getBanking().entrySet()) {
            rowNum = createBankingRows(rowNum, sheet, entry.getKey(), entry.getValue());
        }

        return sheet;
    }

    private static void createBalanceRows(XSSFSheet sheet, ReportBalance balances) {
        Row topRow = sheet.createRow(0);
        createAndSetStringCell(topRow, 0, "Balances");
        Row titleRow = sheet.createRow(1);
        createTitleRow(titleRow, "Currency", "Total Amount", "Total Cashback Amount", "Total Cashback Count", "Total Gratuity Amount", "Total Gratuity Count", "Total Refunds Amount", "Total Refunds Count", "Total Sales Amount", "Total Sales Count", "Totals Since");

        Row amountsRow = sheet.createRow(2);
        createAndSetStringCell(amountsRow, 0, balances.getCurrency());
        createAndSetMoneyCell(amountsRow, 1, balances.getTotalAmount());
        createAndSetMoneyCell(amountsRow, 2, balances.getTotalCashbackAmount());
        createAndSetIntCell(amountsRow, 3, balances.getTotalCashbackCount());
        createAndSetMoneyCell(amountsRow, 4, balances.getTotalGratuityAmount());
        createAndSetIntCell(amountsRow, 5, balances.getTotalGratuityCount());
        createAndSetMoneyCell(amountsRow, 6, balances.getTotalRefundsAmount());
        createAndSetIntCell(amountsRow, 7, balances.getTotalRefundsCount());
        createAndSetMoneyCell(amountsRow, 8, balances.getTotalSalesAmount());
        createAndSetIntCell(amountsRow, 9, balances.getTotalSalesCount());
        createAndSetStringCell(amountsRow, 10, balances.getTotalsSince());
    }

    private static int createTotalsRows(int rowNum, String title, XSSFSheet sheet, Map<String,TotalsBreakdown> totals) {
        Row topRow = sheet.createRow(rowNum++);
        createAndSetStringCell(topRow, 0, title);
        Row titleRow = sheet.createRow(rowNum++);
        createTitleRow(titleRow,"Name", "Currency", "Total Amount", "Total Refund Amount", "Total Refunds Count", "Total Sales Amount", "Total Sales Count");

        for(Map.Entry<String,TotalsBreakdown> entry : totals.entrySet()) {
            Row row = sheet.createRow(rowNum);
            createAndSetStringCell(row, 0, entry.getKey());
            createAndSetStringCell(row, 1, entry.getValue().getCurrency());
            createAndSetMoneyCell(row, 2, entry.getValue().getTotalAmount());
            createAndSetMoneyCell(row, 3, entry.getValue().getTotalRefundAmount());
            createAndSetIntCell(row, 4, entry.getValue().getTotalRefundsCount());
            createAndSetMoneyCell(row, 5, entry.getValue().getTotalSalesAmount());
            createAndSetIntCell(row, 6, entry.getValue().getTotalSalesCount());

            rowNum++;
        }

        return rowNum;
    }

    private static int createBankingRows(int rowNum, XSSFSheet sheet, String acquirerName, Acquirer acquirer) {
        Row topRow = sheet.createRow(rowNum++);
        createAndSetStringCell(topRow, 0, acquirerName);

        rowNum = createTotalsRows(rowNum, "Current Session Issuer Totals", sheet, acquirer.getCurrentSessionIssuerTotals());
        rowNum++;
        rowNum = createTotalsRows(rowNum, "Current Session Totals", sheet, Collections.singletonMap("", acquirer.getCurrentSessionTotals()));
        rowNum++;
        rowNum = createTotalsRows(rowNum, "Previous Session Issuer Totals", sheet, acquirer.getPreviousSessionIssuerTotals());
        rowNum++;
        rowNum = createTotalsRows(rowNum, "Previous Session Totals", sheet, Collections.singletonMap("", acquirer.getPreviousSessionTotals()));
        rowNum++;

        Row sessionNumbersRow = sheet.createRow(rowNum++);
        createAndSetStringCell(sessionNumbersRow, 0, "Previous Transaction Numbers");
        createAndSetStringCell(sessionNumbersRow, 1, "Current Transaction Numbers");
        List<String> previousSessionTransactionNumbers = acquirer.getPreviousSessionTransactionNumbers();
        List<String> currentSessionTransactionNumbers = acquirer.getCurrentSessionTransactionNumbers();
        for(int i = 0; i < Math.max(currentSessionTransactionNumbers.size(), previousSessionTransactionNumbers.size()); i++) {
            rowNum += i;
            Row transactionRow = sheet.createRow(rowNum);
            if(i < previousSessionTransactionNumbers.size()) {
                createAndSetStringCell(transactionRow, 0, previousSessionTransactionNumbers.get(i));
            }
            if(i < currentSessionTransactionNumbers.size()) {
                createAndSetStringCell(transactionRow, 1, currentSessionTransactionNumbers.get(i));
            }
        }

        return rowNum;
    }

    private static void createTitleRow(Row titleRow, String... titles) {
        for(int i = 0; i < titles.length; i++) {
            createAndSetStringCell(titleRow, i, titles[i]);
        }
    }

    private static void createAndSetStringCell(Row row, int column, String value) {
        row.createCell(column).setCellValue(value);
    }

    private static void createAndSetMoneyCell(Row row, int column, int value) {
        row.createCell(column).setCellValue(String.format("%.2f",MoneyService.toMoneyRoundNearest(value)));
    }

    private static void createAndSetIntCell(Row row, int column, int value) {
        row.createCell(column).setCellValue(value);
    }
}
