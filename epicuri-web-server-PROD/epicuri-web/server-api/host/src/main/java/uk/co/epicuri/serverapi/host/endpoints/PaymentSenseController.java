package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.ReportType;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.TerminalList;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/External/PaymentSense", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class PaymentSenseController {
    @Autowired
    private PaymentSenseRestService paymentSenseRestService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @HostAuthRequired
    @RequestMapping(value = "/terminals", method = RequestMethod.GET)
    public ResponseEntity<?> getPSTerminals(@RequestHeader(Params.AUTHORIZATION) String token) {
        Restaurant restaurant = getRestaurant(token);
        if(restaurant == null || !restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            return ResponseEntity.notFound().build();
        }

        TerminalList terminalList = paymentSenseRestService.getTerminals(restaurant);
        if(terminalList == null) { //might happen if no terminals are online
            terminalList = new TerminalList();
        }
        return ResponseEntity.ok(terminalList);

    }

    private Restaurant getRestaurant(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        return masterDataService.getRestaurant(restaurantId);
    }

    @HostAuthRequired
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public ResponseEntity<?> getPSReports(@RequestHeader(Params.AUTHORIZATION) String token) {
        Restaurant restaurant = getRestaurant(token);
        if(restaurant == null || !restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            return ResponseEntity.notFound().build();
        }

        List<PaymentSenseReport> paymentSenseReports = paymentSenseRestService.getPaymentSenseReports(restaurant.getId());
        paymentSenseReports.sort((c1,c2) -> Long.compare(c2.getTime(),c1.getTime()));
        List<PACReport> reports = new ArrayList<>();
        for(PaymentSenseReport paymentSenseReport : paymentSenseReports) {
            reports.addAll(paymentSenseReport.getPACReports());
        }
        return ResponseEntity.ok(reports);
    }

    @HostAuthRequired
    @RequestMapping(value = "/reports/{tpi}", method = RequestMethod.POST)
    public ResponseEntity<?> postReportRequest(@RequestHeader(Params.AUTHORIZATION) String token,
                                               @NotNull @RequestBody ReportType reportType,
                                               @PathVariable("tpi") String tpi) {
        Restaurant restaurant = getRestaurant(token);
        if(restaurant == null || !restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            return ResponseEntity.notFound().build();
        }

        PaymentSenseReport paymentSenseReport = paymentSenseRestService.postReportAndPollOnTerminal(restaurant.getId(), restaurant.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE), tpi, reportType);
        if(paymentSenseReport == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(paymentSenseReport);
    }
}
