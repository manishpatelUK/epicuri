package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PaymentSenseReportingUtilTest {


    @Test
    public void toWorkBook() throws Exception {
        List<PACReport> patReport = createPATReport();
        XSSFWorkbook workbook = PaymentSenseReportingUtil.toWorkBook(patReport);
        /*File tempFile = File.createTempFile("test", ".xls");
        System.out.println(tempFile);

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        workbook.write(fileOutputStream);*/
    }

    private List<PACReport> createPATReport() {
        List<PACReport> pacReports = new ArrayList<>();
        pacReports.add(createReport());
        pacReports.add(createReport());
        return pacReports;
    }

    public static PACReport createReport() {
        PACReport patReport = new PACReport();
        patReport.setReportTime("2017-11-09T13:40:33.233Z");
        patReport.setReportType("TEST");

        patReport.setBalances(createReportBalances());
        patReport.getBanking().put("bank1", createAcquirer());
        patReport.getBanking().put("bank2", createAcquirer());
        patReport.getBanking().put("bank3", createAcquirer());

        return patReport;
    }

    private static Acquirer createAcquirer() {
        Acquirer acquirer = new Acquirer();
        acquirer.setCurrency("GBP");
        acquirer.getCurrentSessionIssuerTotals().put("issuer1", createBreakdown());
        acquirer.getCurrentSessionIssuerTotals().put("issuer2", createBreakdown());
        acquirer.getPreviousSessionIssuerTotals().put("issuer1", createBreakdown());
        acquirer.getPreviousSessionIssuerTotals().put("issuer2", createBreakdown());
        acquirer.setCurrentSessionTotals(createBreakdown());
        acquirer.setPreviousSessionTotals(createBreakdown());

        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            list.add(RandomStringUtils.randomAlphanumeric(4));
        }
        acquirer.setCurrentSessionTransactionNumbers(list);
        acquirer.setPreviousSessionTransactionNumbers(list);

        return acquirer;
    }

    private static TotalsBreakdown createBreakdown() {
        TotalsBreakdown totalsBreakdown = new TotalsBreakdown();
        totalsBreakdown.setCurrency("GBP");
        totalsBreakdown.setTotalAmount(RandomUtils.nextInt(0,1000));
        totalsBreakdown.setTotalRefundAmount(RandomUtils.nextInt(0,1000));
        totalsBreakdown.setTotalRefundsCount(RandomUtils.nextInt(0,1000));
        totalsBreakdown.setTotalSalesAmount(RandomUtils.nextInt(0,1000));
        totalsBreakdown.setTotalSalesCount(RandomUtils.nextInt(0,1000));
        return totalsBreakdown;
    }

    private static ReportBalance createReportBalances() {
        ReportBalance reportBalance = new ReportBalance();
        reportBalance.setCurrency("GBP");
        reportBalance.getIssuerTotals().put("issuer1", createBreakdown());
        reportBalance.getIssuerTotals().put("issuer2", createBreakdown());
        reportBalance.getWaiterTotals().put("waiter1", createBreakdown());
        reportBalance.getWaiterTotals().put("waiter2", createBreakdown());
        reportBalance.setTotalAmount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalCashbackAmount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalCashbackCount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalGratuityAmount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalGratuityCount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalRefundsAmount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalRefundsCount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalSalesAmount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalSalesCount(RandomUtils.nextInt(0,1000));
        reportBalance.setTotalsSince("2017-11-09T13:40:33.233Z");
        return reportBalance;
    }
}