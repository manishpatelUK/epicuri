package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;
import uk.co.epicuri.serverapi.common.pojo.model.session.CalculationKey;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.repository.PaymentSenseReportRepository;
import uk.co.epicuri.serverapi.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by manish on 28/07/2017.
 */
@Service
public class PaymentSenseRestService extends ExternalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentSenseRestService.class);

    private RestTemplate restTemplate = new RestTemplate();
    private static final String PS_TABLE_ENDPOINT = "/pat/tables";
    private static final String PS_REPORTS = "reports";
    private static final String PS_PAT_REPORTS_ENDPOINT = "/pat/" + PS_REPORTS;
    private static final String PS_TERMINALS_ENDPOINT = "/pac/terminals";

    private static final String STAFF_ID = "epicuriadmin";

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PaymentSenseReportRepository paymentSenseReportRepository;

    public PaymentSenseRestService() {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return clientHttpResponse.getStatusCode().is4xxClientError() || clientHttpResponse.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
                LOGGER.trace("Error from PaymentSense: {}", IOUtils.toString(clientHttpResponse.getBody()));
            }
        });
    }

    public boolean postTable(String staffId, Restaurant restaurant, String table, int initialAmount) {
        LOGGER.trace("Post table: {},{},{},{}", staffId, restaurant.getId(), table, initialAmount);
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return false;

        PostTableRequest request = new PostTableRequest();
        request.setTableName(table);
        request.setAmount(initialAmount);
        request.setCurrency(restaurant.getISOCurrency());
        request.getWaiterIds().add(staffId);

        HttpHeaders headers = createStandardJsonHeaders();
        headers.set("Authorization", getBasicAuthentication(staffId,kvData.getKey()));
        HttpEntity<PostTableRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PostTableResponse> responseEntity = restTemplate.exchange(
                    kvData.getHost() + PS_TABLE_ENDPOINT,
                    HttpMethod.PUT, entity, PostTableResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                LOGGER.trace("Table {} post succeeded", table);
                return true;
            } else {
                LOGGER.trace("Could not post table {}, response: {}", table, responseEntity.toString());
                return false;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not post table {}: {}", table, ex);
            return false;
        }
    }

    public boolean deleteTable(String staffId, Restaurant restaurant, String table, int lastKnownAmount, int lastKnownPaidAmount) {
        LOGGER.trace("Delete table: {},{},{},{},{}", staffId, restaurant.getId(), table, lastKnownAmount, lastKnownPaidAmount);
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return false;

        SequentialTableRequest request = new SequentialTableRequest();
        request.setLastKnownPaidAmount(lastKnownPaidAmount);
        request.setLastKnownAmount(lastKnownAmount);

        HttpHeaders headers = createStandardJsonHeaders();
        headers.set("Authorization", getBasicAuthentication(staffId,kvData.getKey()));
        HttpEntity<SequentialTableRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    kvData.getHost() + PS_TABLE_ENDPOINT + "/" + table,
                    HttpMethod.DELETE, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                LOGGER.trace("Table {} delete succeeded", table);
                return true;
            } else {
                LOGGER.trace("Could not delete table {}. Response from PaymentSense: {}", table, responseEntity.getBody());
                return false;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not delete table {}: {}", table, ex);
            return false;
        }
    }

    public void cleanAllTables(Restaurant restaurant, long endTime) {
        List<Session> sessions = sessionService.getLiveSessions(restaurant.getId(), endTime);
        List<String> liveTables = sessions.stream().map(s -> SessionService.getTableName(restaurant,s)).collect(Collectors.toList());

        List<TableResponse> tables = getTables(restaurant);
        LOGGER.trace("Clean {} tables", tables.size());
        for(TableResponse response : tables) {
            if(liveTables.contains(response.getTableName())) {
                deleteTable("1", restaurant, response.getTableName(), response.getAmount(), response.getAmountPaid());
            }
        }
    }

    public TableResponse getTable(Restaurant restaurant, String table) {
        LOGGER.trace("Get table: {}", table);
        KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE);
        if(isInvalid(kvData)) {
            LOGGER.trace("Invalid kv data");
            return new TableResponse();
        }

        HttpEntity<String> request = getHttpHeaders(kvData);

        try {
            ResponseEntity<TableResponse> tableResponse = restTemplate.exchange(kvData.getHost() + "/" + PS_TABLE_ENDPOINT + "/" + table,
                    HttpMethod.GET, request, TableResponse.class);
            if(tableResponse.getStatusCode() == HttpStatus.OK) {
                LOGGER.trace("Get table succeeded: {}", table);
                return tableResponse.getBody();
            } else {
                LOGGER.trace("Could not get table payments, response: {}", tableResponse.getBody());
                return new TableResponse();
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get table payments, response: {}", ex);
        }
        return new TableResponse();
    }

    public List<TableResponse> getTables(Restaurant restaurant) {
        LOGGER.trace("Get table for restaurant: {}", restaurant.getId());
        KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE);
        if(isInvalid(kvData)) {
            LOGGER.trace("Invalid kv data");
            return new ArrayList<>();
        }

        HttpEntity<String> request = getHttpHeaders(kvData);

        try {
            ResponseEntity<TableResponseList> tableResponse = restTemplate.exchange(kvData.getHost() + "/" + PS_TABLE_ENDPOINT,
                    HttpMethod.GET, request, TableResponseList.class);
            if(tableResponse.getStatusCode() == HttpStatus.OK && tableResponse.getBody() != null) {
                LOGGER.trace("Get tables succeeded: {}", tableResponse);
                return tableResponse.getBody().getTables();
            } else if(tableResponse.getStatusCode() == HttpStatus.OK) {
                return new ArrayList<>();
            } else {
                LOGGER.trace("Could not get table payments, response: {}", tableResponse.getBody());
                return new ArrayList<>();
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get table payments, response: {}", ex);
        }
        return new ArrayList<>();
    }

    public boolean updateAmount(String staffId, Restaurant restaurant, String table, int lastKnownAmount, int lastKnownPaidAmount, int amount) {
        LOGGER.trace("Update table: {}, {}, {}, {}, {}, {}", staffId, restaurant.getId(), table, lastKnownAmount, lastKnownPaidAmount, amount);
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return false;

        AmountUpdateRequest request = new AmountUpdateRequest();
        request.setLastKnownAmount(lastKnownAmount);
        request.setNewAmount(amount);
        request.setLastKnownPaidAmount(lastKnownPaidAmount);

        HttpHeaders headers = createStandardJsonHeaders();
        headers.set("Authorization", getBasicAuthentication(staffId,kvData.getKey()));
        HttpEntity<AmountUpdateRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    kvData.getHost() + PS_TABLE_ENDPOINT + "/" + table + "/amount",
                    HttpMethod.PUT, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                LOGGER.trace("Update table succeeded");
                return true;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not update table {}: {}", table, ex);
            return false;
        }

        return false;
    }

    public boolean lock(String staffId, Restaurant restaurant, String table, int lastKnownAmount, int lastKnownPaidAmount, boolean lock, boolean force) {
        LOGGER.trace("Update table: {}, {}, {}, {}, {}", staffId, restaurant.getId(), table, lastKnownAmount, lastKnownPaidAmount);
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return false;

        LockUpdateRequest request = new LockUpdateRequest();
        request.setLastKnownAmount(lastKnownAmount);
        request.setLastKnownPaidAmount(lastKnownPaidAmount);
        request.setLocked(lock);
        request.setOverride(force);
        request.setLockedBy(staffId);

        HttpHeaders headers = createStandardJsonHeaders();
        headers.set("Authorization", getBasicAuthentication(staffId,kvData.getKey()));
        HttpEntity<LockUpdateRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    kvData.getHost() + PS_TABLE_ENDPOINT + "/" + table + "/lock",
                    HttpMethod.PUT, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                LOGGER.trace("Update lock succeeded");
                return true;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not lock table {}: {}", table, ex);
            return false;
        }

        return false;
    }

    public boolean updateReceipt(String staffId, Restaurant restaurant, String table, int lastKnownAmount, int lastKnownPaidAmount, List<Order> orders) {
        LOGGER.trace("Update receipt: {}, {}, {}, {}, {}", staffId, restaurant.getId(), table, lastKnownAmount, lastKnownPaidAmount);
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return false;

        if(orders.size() == 0) {
            return true;
        }

        ReceiptBuilder builder = ReceiptBuilder.newInstance().setLastKnownAmount(lastKnownAmount).setLastKnownPaidAmount(lastKnownPaidAmount)
                .add("Billed Items", ReceiptLineFormat.BOLD, ReceiptLineFormat.DOUBLE_HEIGHT)
                .add("-", ReceiptLineType.LINE_SEPARATOR_SINGLE);

        for(Order order : orders) {
            double amount = MoneyService.toMoneyRoundNearest(SessionCalculationService.getOrderValue(order));
            builder.add(order.getMenuItem().getName() + "  " + String.format("%.2f", amount));
        }

        Session session = sessionService.getSession(orders.get(0).getSessionId());
        Map<CalculationKey,Number> calculatedValues = sessionCalculationService.calculateValues(session);
        builder.add("-", ReceiptLineType.LINE_SEPARATOR_SINGLE)
                .add("Subtotal          " + String.format("%.2f", MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS).intValue())))
                .add("Discounts         " + String.format("%.2f", MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.DISCOUNT_TOTAL).intValue())))
                .add("Tip               " + String.format("%.2f", MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TIP_TOTAL).intValue())))
                .add("REMAINING         " + String.format("%.2f", MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.REMAINING_TOTAL).intValue())), ReceiptLineFormat.DOUBLE_HEIGHT)
                .add("TOTAL             " + String.format("%.2f", MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TOTAL).intValue())), ReceiptLineFormat.DOUBLE_HEIGHT);

        Receipt request = builder.build();

        HttpHeaders headers = createStandardJsonHeaders();
        headers.set("Authorization", getBasicAuthentication(staffId,kvData.getKey()));
        HttpEntity<Receipt> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    kvData.getHost() + PS_TABLE_ENDPOINT + "/" + table + "/receipt",
                    HttpMethod.PUT, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                LOGGER.trace("Update receipt succeeded");
                return true;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not put receipt on table {}: {}", table, ex);
            return false;
        }
        return false;
    }

    public PACReport getReport(String staffId, Restaurant restaurant) {
        LOGGER.trace("Get report: {}, {}", staffId, restaurant.getId());
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return null;

        HttpEntity<String> request = getHttpHeaders(kvData);

        try {
            ResponseEntity<PACReport> response = restTemplate.exchange(kvData.getHost() + "/" + PS_PAT_REPORTS_ENDPOINT,
                    HttpMethod.GET, request, PACReport.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                LOGGER.trace("Got report: {}", response);
                return response.getBody();
            } else {
                LOGGER.trace("Could not reports, response: {}", response.getBody());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get reports, response: {}", ex);
        }
        return null;
    }

    public TerminalList getTerminals(Restaurant restaurant) {
        KVData kvData = validateKVData(restaurant);
        if (kvData == null) return null;

        HttpEntity<String> request = getHttpHeaders(kvData);

        try {
            ResponseEntity<TerminalList> response = restTemplate.exchange(kvData.getHost() + "/" + PS_TERMINALS_ENDPOINT,
                    HttpMethod.GET, request, TerminalList.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                LOGGER.trace("Could not terminals, response: {}", response.getBody());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get terminals, response: {}", ex);
        }
        return null;
    }

    private KVData validateKVData(Restaurant restaurant) {
        KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE);
        if (isInvalid(kvData)) {
            LOGGER.trace("Invalid kv data");
            return null;
        }
        return kvData;
    }

    public PACReport getPACReport(KVData kvData, String tpi, String requestId) {
        HttpHeaders headers = createHttpHeaders(kvData);
        headers.setAccept(Collections.singletonList(new MediaType("application","connect.v1+json")));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<PACReport> response = restTemplate.exchange(kvData.getHost() + "/" + PS_TERMINALS_ENDPOINT + "/" + tpi + "/reports/" + requestId,
                    HttpMethod.GET, request, PACReport.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                LOGGER.trace("Could not PACReport, response: {}", response.getBody());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get PACReport, response: {}", ex);
        }
        return null;
    }

    public List<PaymentSenseReport> getPaymentSenseReports(String restaurantId) {
        return paymentSenseReportRepository.findByRestaurantId(restaurantId);
    }

    private HttpEntity<String> getHttpHeaders(KVData kvData) {
        HttpHeaders headers = createHttpHeaders(kvData);
        return new HttpEntity<>(headers);
    }

    private HttpHeaders createHttpHeaders(KVData kvData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getBasicAuthentication(STAFF_ID, kvData.getKey()));
        return headers;
    }

    public PaymentSenseReport postReportAndPollOnTerminal(String restaurantId, KVData kvData, String tpi, ReportType reportType) {
        PaymentSenseReportRequestResponse response = postReportTerminal(kvData, tpi, reportType.getReportType());
        if(response == null || StringUtils.isBlank(response.getRequestId())) {
            return null;
        }
        int counter = 0;

        PACReport pacReport = null;
        while(counter <= 120) {
            pacReport = getPACReport(kvData, tpi, response.getRequestId());
            if(pacReport != null && pacReport.getNotifications() != null && pacReport.getNotifications().contains("REPORT_FINISHED")) {
                break;
            }
            counter++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }

        PaymentSenseReport paymentSenseReport = new PaymentSenseReport();
        paymentSenseReport.setRestaurantId(restaurantId);
        paymentSenseReport.setPACReports(Collections.singletonList(pacReport));
        paymentSenseReportRepository.insert(paymentSenseReport);
        return paymentSenseReport;
    }

    public PaymentSenseReportRequestResponse postReportTerminal(KVData kvData, String tpi, String reportType) {
        HttpHeaders headers = createHttpHeaders(kvData);
        headers.setAccept(Collections.singletonList(new MediaType("application","connect.v1+json")));
        HttpEntity<String> request = new HttpEntity<>("{\"reportType\":\""+ reportType + "\"}", headers);

        try {
            ResponseEntity<PaymentSenseReportRequestResponse> response = restTemplate.exchange(kvData.getHost() + "/" + PS_TERMINALS_ENDPOINT + "/" + tpi + "/" + PS_REPORTS,
                    HttpMethod.POST, request, PaymentSenseReportRequestResponse.class);

            if(response.getStatusCode() == HttpStatus.CREATED) {
                LOGGER.trace("Got report: {}", response);
                return response.getBody();
            } else {
                LOGGER.trace("Could not reports, response: {}", response.getBody());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.trace("Could not get reports, response: {}", ex);
        }
        return null;
    }

    private boolean isInvalid(KVData kvData) {
        return kvData == null || StringUtils.isBlank(kvData.getKey()) || StringUtils.isBlank(kvData.getHost());
    }
}
