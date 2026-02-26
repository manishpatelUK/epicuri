package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.CSVWrapper;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.ReportingConstraints;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.engines.reporting.reports.*;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.external.EmailService;
import uk.co.epicuri.serverapi.service.external.PaymentSenseReportingUtil;
import uk.co.epicuri.serverapi.service.reporting.ReportingService;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.co.epicuri.serverapi.spring.CsvMessageConverter.TEXT_CSV;

/**
 * Created by manish
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/Reporting", consumes = MediaType.ALL_VALUE)
public class ReportingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingController.class);

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handleOptions() {
        return new ResponseEntity(HttpStatus.OK);
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/customerDetails{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getCustomerDetails(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<CustomerDetailsReportLine> lines = reportingService.getCustomerDetailsReportLines(reportingConstraints);
        LOGGER.trace("customerDetails -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, CustomerDetailsReportLine.class, "Customers.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/itemsAggregated{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getItemsAggregated(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestParam("start") String start,
                                         @RequestParam("end") String end,
                                         @RequestParam(value = "byPLU", defaultValue = "false") boolean byPlu) {
        //item id, item name, price, last sold, qty, value including mods, value excluding mods, avg sales price, type, tax % & name
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        reportingConstraints.setAggregateByPLU(byPlu);
        List<AggregatedItemsReportLine> lines = reportingService.getAggregatedItemsReportLines(reportingConstraints);
        LOGGER.trace("itemsAggregated -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, AggregatedItemsReportLine.class, "Aggregated_Menu_Items.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/payments{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getPayments(@RequestHeader(Params.AUTHORIZATION) String token,
                                  @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(reportingConstraints);
        LOGGER.trace("payments -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, AdjustmentReportLine.class, "Payments_and_Discounts.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/itemDetails{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getItemDetails(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<ItemDetailsReportLine> lines = reportingService.getItemDetailsReportLines(reportingConstraints);
        LOGGER.trace("itemDetails -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, ItemDetailsReportLine.class, "Item_Details.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/revenues{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getRevenues(@RequestHeader(Params.AUTHORIZATION) String token,
                                  @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<RevenueReportLine> lines = reportingService.getRevenueReportLines(reportingConstraints);
        LOGGER.trace("revenues -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, RevenueReportLine.class, "Revenues.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/reservations{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getReservations(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<ReservationLine> lines = reportingService.getReservationLines(reportingConstraints);
        LOGGER.trace("reservations -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, ReservationLine.class, "Reservations.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/cashups{.csv}", method = RequestMethod.GET, produces = TEXT_CSV)
    public CSVWrapper getCashups(@RequestHeader(Params.AUTHORIZATION) String token,
                                 @RequestParam("start") String start, @RequestParam("end") String end) {
        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, start, end);
        List<CashUpReportLine> lines = reportingService.getCashupLines(reportingConstraints);
        LOGGER.trace("cashups -> {} rows", lines.size());

        return reportingService.createCsvWrapper(lines, CashUpReportLine.class, "Cashups.csv");
    }

    @HostAuthRequired
    @ResponseBody
    @RequestMapping(value = "/paymentSense/email/{requestId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEmailPaymentSensePAT(@RequestHeader(Params.AUTHORIZATION) String token, @PathVariable("requestId") String requestId) {
        if(StringUtils.isBlank(requestId)) {
            return ResponseEntity.badRequest().body("Request ID has not been supplied - cannot find report");
        }

        ReportingConstraints reportingConstraints = reportingService.createConstraints(token, null, null);
        Restaurant restaurant = masterDataService.getRestaurant(reportingConstraints.getRestaurantId());
        PACReport paymentSensePACReport = reportingService.getPaymentSensePACReport(reportingConstraints.getRestaurantId(), requestId);

        if(StringUtils.isBlank(restaurant.getInternalEmailAddress())) {
            return ResponseEntity.badRequest().body("Bad configuration - venue does not have an internal email address set up");
        }

        if(paymentSensePACReport == null) {
            return ResponseEntity.notFound().build();
        }

        XSSFWorkbook workbook = PaymentSenseReportingUtil.toWorkBook(Lists.newArrayList(paymentSensePACReport));
        File file = null;
        try {
            file = File.createTempFile("paymentSenseReport",".xls");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();

            boolean built = emailService.newHTMLMailBuilder()
                    .from("info@epicuri.co.uk")
                    .to(restaurant.getInternalEmailAddress())
                    .subject("PaymentSense Reports")
                    .body("Please see attached for requested PS report")
                    .attach(file)
                    .build();

            if(!built) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not email report");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not get report");
        } finally {
            if(file != null) {
                file.delete();
            }
        }

        return ResponseEntity.ok().build();
    }


}
