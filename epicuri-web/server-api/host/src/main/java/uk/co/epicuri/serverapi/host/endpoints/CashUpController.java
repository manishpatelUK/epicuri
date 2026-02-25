package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.EpicuriAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.CashUpAggregator;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/CashUp", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CashUpController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashUpController.class);

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Value("${epicuri.waiter.cashup.window}")
    private long checkWindow;

    public CashUpController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<CashUpResponse>> getCashUps(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);

        //limit this to last 12 months
        List<CashUp> cashUps = archiveDataService.getLastCashUps(restaurantId, System.currentTimeMillis()- checkWindow);
        List<CashUpResponse> response = cashUps.stream().map(CashUpResponse::new).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @HostAuthRequired
    @RequestMapping(value = "/IsOkToCashup", method = RequestMethod.GET)
    public ResponseEntity<?> isOkToCashup(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);

        List<Session> sessions = sessionService.getLiveSessions(restaurantId);
        if (sessions.size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All open sessions must be closed before cash-up");
        }

        StringMessage message = new StringMessage();
        message.setMessage("OK To Cash up");

        return ResponseEntity.ok(message);
    }

    @HostAuthRequired
    @RequestMapping(value = "/CheckStatus", method = RequestMethod.GET)
    public ResponseEntity<?> isCheckStatus(@RequestHeader(Params.AUTHORIZATION) String token) {
        return isOkToCashup(token);
    }

    @HostAuthRequired
    @RequestMapping(value = "/Simulate", method = RequestMethod.POST)
    public ResponseEntity<CashUpResponse> postSimulate(@RequestHeader(Params.AUTHORIZATION) String token,
                                                       @NotNull @RequestBody CashUpRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if (StringUtils.isBlank(restaurantId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        long startTime = getStartTime(request, restaurantId);
        long now = System.currentTimeMillis();
        long endTime = getEndTime(request, now);
        CashUpAggregator aggregator = createCashUpAggregatorAndClearSessions(restaurantId, startTime, endTime, true);
        aggregator.aggregate();
        CashUpResponse response = new CashUpResponse(startTime, endTime, aggregator.getReportValues(), aggregator.getPaymentReport(), aggregator.getAdjustmentReport(), aggregator.getItemAdjustmentLossReport(), aggregator.getRefundValues(), aggregator.getRefundPaymentValues());
        response.setWrapUp(true);
        return ResponseEntity.ok(response);
    }

    private long getEndTime(CashUpRequest request, long now) {
        return (request.getEndTime() * 1000) > now ? now : request.getEndTime() * 1000;
    }

    private long getStartTime(CashUpRequest request, String restaurantId) {
        long startTime;
        CashUp cashUp = archiveDataService.getLastCashUp(restaurantId);
        if(cashUp == null) {
            startTime = masterDataService.getRestaurant(restaurantId).getCreationTime();
        } else {
            if (request.getStartTime() == null) {
                startTime = cashUp.getEndTime() + 1;
            } else {
                startTime = request.getStartTime() * 1000;
            }
        }
        return startTime;
    }


    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<CashUpResponse> postCashUp(@RequestHeader(Params.AUTHORIZATION) String token,
                                                     @NotNull @RequestBody CashUpRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if (StringUtils.isBlank(restaurantId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        long startTime = getStartTime(request, restaurantId);
        long now = System.currentTimeMillis();
        long endTime = getEndTime(request, now);


        CashUpAggregator aggregator = createCashUpAggregatorAndClearSessions(restaurantId, startTime, endTime, false);
        aggregator.aggregate();

        CashUpResponse response = new CashUpResponse(startTime, endTime, aggregator.getReportValues(), aggregator.getPaymentReport(), aggregator.getAdjustmentReport(), aggregator.getItemAdjustmentLossReport(), aggregator.getRefundValues(), aggregator.getRefundPaymentValues());

        CashUp cashUp = new CashUp(restaurantId, startTime, endTime,
                aggregator.getReportValues(),
                aggregator.getPaymentReport(),
                aggregator.getAdjustmentReport(),
                aggregator.getItemAdjustmentLossReport(),
                aggregator.getSessionIds(),
                aggregator.getRefundValues(),
                aggregator.getRefundPaymentValues());
        cashUp.setUnfulfilledCheckIns(aggregator.getUnfulfilledCheckIns());
        cashUp = archiveDataService.addCashUp(cashUp);
        response.setId(cashUp.getId());
        sessionService.updateCashUpId(aggregator.getSessionIds(), cashUp.getId());
        asyncOrderHandlerService.onCashUp(masterDataService.getRestaurant(restaurantId), cashUp, aggregator);

        return ResponseEntity.ok(response);
    }

    private CashUpAggregator createCashUpAggregatorAndClearSessions(String restaurantId, long startTime, long endTime, boolean isSimulation) {
        LOGGER.trace("Get sessions between {}-{}", startTime, endTime);
        List<Session> sessions = sessionService.getSessionsBetweenClosingTimes(restaurantId, startTime, endTime)
                .stream().filter(s -> {
                    if(isSimulation) {
                        return StringUtils.isBlank(s.getCashUpId());
                    } else {
                        return s.getClosedTime() != null && StringUtils.isBlank(s.getCashUpId());
                    }
                }).collect(Collectors.toList());
        //include live sessions if isSimulation
        if(isSimulation) {
            Set<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toSet());
            sessions.addAll(sessionService.getLiveSessions(restaurantId, endTime).stream().filter(s -> !sessionIds.contains(s.getId())).collect(Collectors.toList()));
        }

        LOGGER.trace("Consider {} sessions", sessions.size());
        Map<String,List<Order>> ordersBySession = liveDataService.getOrdersBySessionIds(sessions.stream().map(Session::getId).collect(Collectors.toList()));

        CashUpAggregator aggregator = new CashUpAggregator(sessionCalculationService);

        for(Session session : sessions) {
            if(session.isRemoveFromReports()) {
                continue;
            }
            aggregator.addSession(session, ordersBySession.getOrDefault(session.getId(), new ArrayList<>()));
        }

        if(!isSimulation) {
            for (Session session : sessions) {
                if(!session.isRemoveFromReports()) {
                    sessionPaymentService.ensurePreAuthsAreProcessed(session);
                }
                sessionService.clearWithSession(session, true, true, true, true, true);
            }
        }

        //clear up dirty checkIns
        List<CheckIn> checkIns = liveDataService.getCheckIns(restaurantId).stream().filter(c -> c.getTime() >= startTime && c.getTime() <= endTime).collect(Collectors.toList());
        if(!isSimulation) {
            LOGGER.trace("Delete {} checkins, all will be without sessions attached", checkIns.size());
            liveDataService.deleteCheckIns(checkIns);
        }
        aggregator.setUnfulfilledCheckIns(checkIns.stream().map(CheckIn::getTime).collect(Collectors.toList()));

        return aggregator;
    }

}
