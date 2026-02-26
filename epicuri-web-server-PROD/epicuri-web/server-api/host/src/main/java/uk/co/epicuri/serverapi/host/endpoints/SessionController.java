package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.errors.ResourceNotFoundException;
import uk.co.epicuri.serverapi.service.SessionCalculationService;
import uk.co.epicuri.serverapi.service.SessionTimingService;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Session", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionTimingService timingService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getAllActiveSessions(@RequestHeader(Params.AUTHORIZATION) String token) {
        return getAllSessions(token);
    }

    @HostAuthRequired
    @RequestMapping(value = "/All", method = RequestMethod.GET)
    public ResponseEntity<?> getAllSessions(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);

        return ResponseEntity.ok(sessionService.getAllSessions(restaurantId));
    }

    @HostAuthRequired
    @RequestMapping(value = "/ConvertAdHocToTab", method = RequestMethod.POST)
    public ResponseEntity<?> postConvertAdHocToTab(@NotNull @RequestBody PartyUpdateRequest partyUpdateRequest) {
        Session session = sessionService.getSession(partyUpdateRequest.getSessionId());
        if(session == null || session.getSessionType() != SessionType.ADHOC) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(partyUpdateRequest.getUpdate() == null || StringUtils.isBlank(partyUpdateRequest.getUpdate().getServiceId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }

        Service service = masterDataService.getService(partyUpdateRequest.getUpdate().getServiceId());
        if(service == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }

        if(session.getService() != null && !session.getService().getId().equals(service.getId())) {
            sessionService.updateService(session.getId(), service);
        }

        if(session.getDiners().size() == 0) { //should never really happen but just in case
            Diner diner = new Diner(session);
            diner.setDefaultDiner(true);
            diner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
            session.getDiners().add(diner);
        } else if(session.getDiners().size() == 1) {
            Diner diner = session.getDiners().get(0);
            diner.setDefaultDiner(true);
            diner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
        }

        int numberOfActualDiners = session.getDiners().size()-1;
        int requiredDiners = partyUpdateRequest.getUpdate().getNumberOfPeople() - numberOfActualDiners;

        for(int i = 0; i < requiredDiners; i++) {
            Diner diner = new Diner(session);
            session.getDiners().add(diner);
            diner.setName("Guest " + (i+1));
            if(i == 0
                    && partyUpdateRequest.getUpdate().getLeadCustomer() != null
                    && StringUtils.isNotBlank(partyUpdateRequest.getUpdate().getLeadCustomer().getId())) {
                if(customerService.exists(partyUpdateRequest.getUpdate().getLeadCustomer().getId())) {
                    diner.setCustomerId(partyUpdateRequest.getUpdate().getLeadCustomer().getId());
                }
            }
        }

        session.setSessionType(SessionType.TAB);

        if(!session.getName().equals(partyUpdateRequest.getUpdate().getName())) {
            session.setName(partyUpdateRequest.getUpdate().getName());
            Party party = session.getOriginalParty();
            if(party != null) {
                party.setName(partyUpdateRequest.getUpdate().getName());
                liveDataService.upsert(party);
            }
        }

        sessionService.upsert(session);

        return ResponseEntity.accepted().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/NotInCashup", method = RequestMethod.GET)
    public ResponseEntity<?> getAllSessionsNotInCashUp(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);

        return ResponseEntity.ok(sessionService.getClosedSessions(restaurantId));
    }

    @HostAuthRequired
    @RequestMapping(value = "/Accept/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putAccept(@PathVariable("id") String id) {
        Session session  = sessionService.getSession(id);
        if(session == null || session.getSessionType() != SessionType.TAKEAWAY || StringUtils.isBlank(session.getOriginalBookingId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookingService.acceptBooking(session.getOriginalBookingId());

        return ResponseEntity.ok().build();
    }

    //reject a takeaway
    @HostAuthRequired
    @RequestMapping(value = "/Reject/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putReject(@PathVariable("id") String id,
                                       @RequestBody StringMessage stringMessage) {
        Session session  = sessionService.getSession(id);
        if(session == null || session.getSessionType() != SessionType.TAKEAWAY || StringUtils.isBlank(session.getOriginalBookingId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if(StringUtils.isBlank(stringMessage.getNotice())) {
            stringMessage.setNotice("Apologies. Booking could not be accepted at this time.");
        }

        sessionService.removeFromReports(id);
        if(StringUtils.isNotBlank(session.getOriginalBookingId())) {
            bookingService.rejectBooking(session.getOriginalBookingId(), stringMessage.getNotice());
        }
        sessionPaymentService.ensurePreAuthsAreCancelled(session);
        sessionService.clear(session, true, true, true, true, true);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/RemoveFromReports/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putRemoveFromReports(@PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        sessionService.removeFromReports(session.getId());
        asyncOrderHandlerService.onSessionClose("", masterDataService.getRestaurant(session.getRestaurantId()), session, false);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> checkVoidConditions(Session session, boolean ignoreClosedTime) {
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(!ignoreClosedTime && session.getClosedTime() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session is still open");
        }

        if(StringUtils.isNotBlank(session.getCashUpId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session is in a cash-up");
        }

        if(session.isRemoveFromReports()) {
            return ResponseEntity.status(HttpStatus.OK).body("Session is already removed from reports");
        }

        return ResponseEntity.ok(session);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<HostSessionView> getSession(@RequestHeader(Params.AUTHORIZATION) String token,
                                                      @PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            throw new ResourceNotFoundException();
        }

        asyncOrderHandlerService.onReconciliationRequest("", authenticationService.getRestaurantId(token), id);

        return ResponseEntity.ok(sessionService.getSessionView(session));
    }

    @HostAuthRequired
    @RequestMapping(value = "/RequestBill/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putRequestBill(@PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session != null) {
            sessionService.requestBill(id);
            sessionService.clear(session, false, false, true, false, false);

            if(session.getSessionType() != SessionType.TAKEAWAY) {
                Notification notification = new Notification();
                notification.setNotificationType(NotificationType.ADHOC);
                notification.setRestaurantId(session.getRestaurantId());
                notification.setSessionId(session.getId());
                notification.setTarget(NotificationConstant.TARGET_WAITER_ACTION.getConstant());
                notification.setText(NotificationConstant.TEXT_BILL_REQUEST.getConstant());
                notification.setTime(System.currentTimeMillis());
                liveDataService.upsert(notification);
            }
        }

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/UnrequestBill/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putUnRequestBill(@PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session != null) {
            sessionService.unRequestBill(id);
            deleteBillRequestNotification(session);
        }
        return ResponseEntity.noContent().build();
    }

    private void deleteBillRequestNotification(Session session) {
        List<Notification> notifications = liveDataService.getUnacknowledgedNotifications(session.getRestaurantId(), session.getId());
        Notification billRequest = notifications.stream()
                .filter(n -> StringUtils.isNotBlank(n.getText())
                        && n.getText().equals(NotificationConstant.TEXT_BILL_REQUEST.getConstant())
                        && n.getNotificationType() == NotificationType.ADHOC).findFirst().orElse(null);
        if(billRequest != null) {
            liveDataService.deleteNotification(billRequest.getId());
        }
    }

    /**
     * Currently (2018.01.13) called from Session History and Ad Hoc screens.
     * When from session history, fourceClose = false --> remove from reports
     * Otherwise forceClose is true
     * @param token
     * @param id
     * @param forceClose
     * @param voidReason
     * @return
     */
    @HostAuthRequired
    @RequestMapping(value = "/Void/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putVoid(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @PathVariable("id") String id,
                                     @RequestParam(value = "forceClose", required = false, defaultValue = "false") boolean forceClose,
                                     @NotNull @RequestBody VoidReasonPayload voidReason) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<?> responseEntity = checkVoidConditions(session, forceClose);
        if(responseEntity.getStatusCode() != HttpStatus.OK && !forceClose) {
            return responseEntity;
        }

        if(session.getVoidReason() != null) {
            return responseEntity;
        }

        String staffId = authenticationService.getStaffId(token);
        sessionService.markClosed(session.getId(), staffId);
        if(forceClose) {
            session = sessionService.voidAllPayments(session, staffId);
        }

        if(!forceClose) {
            // this would be done from session history - i.e. like a session deletion
            sessionService.removeFromReports(session.getId());
        }

        asyncOrderHandlerService.onSessionClose("", masterDataService.getRestaurant(session.getRestaurantId()), session, forceClose);

        VoidReason reason = new VoidReason();
        reason.setDescription(voidReason.getReason() == null ? "" : voidReason.getReason());
        reason.setTime(System.currentTimeMillis());
        reason.setStaffId(staffId);
        sessionService.updateVoidReason(session.getId(), reason);

        sessionPaymentService.ensurePreAuthsAreCancelled(session);
        sessionService.clear(session,false,true,true,true, true);
        List<Order> orders = liveDataService.getOrders(session.getId());
        liveDataService.voidOrders(orders.stream().map(Order::getId).collect(Collectors.toList()));

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Unvoid/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putUnVoid(@PathVariable("id") String id) {
        if(!sessionService.exists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        sessionPaymentService.ensurePreAuthsAreReinstated(sessionService.getSession(id));
        sessionService.updateUnVoid(id);
        sessionService.includeOnReports(id);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Open/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putOpen(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(session.getClosedTime() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session is already open");
        }

        if(StringUtils.isNotBlank(session.getCashUpId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session cannot be re-opened. Locked for reporting.");
        }

        session.setClosedTime(null);
        session.setMarkedAsPaid(false);

        if(session.getSessionType() == SessionType.SEATED) {
            //if someone is already occupying the table that this session was on, turn it into a tab
            if(liveDataService.tablesInUse(restaurantId, session.getTables())) {
                session.getTables().clear();
                session.setSessionType(SessionType.TAB);
            }

            deleteBillRequestNotification(session);
        }

        archiveDataService.unarchiveParty(session.getId());
        sessionService.upsert(session);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/PayBill/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putPayBill(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(!sessionCalculationService.isPaid(session)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session is not paid");
        }

        sessionPaymentService.ensurePreAuthsAreProcessed(session);
        asyncOrderHandlerService.onSessionClose("", masterDataService.getRestaurant(session.getRestaurantId()), session, false);
        sessionService.markPaid(id, true, authenticationService.getStaffId(token));
        sessionService.clear(session,false,false,true,true, true);

        return ResponseEntity.ok(sessionService.getSessionView(sessionService.getSession(session.getId())));
    }

    @HostAuthRequired
    @RequestMapping(value = "/Close/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putClose(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @PathVariable("id") String id,
                                      @NotNull @RequestBody CloseSessionRequest request) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }


        String staffId = authenticationService.getStaffId(token);
        boolean fullyPaid = sessionService.isFullyPaid(session);
        if(!fullyPaid) {
            //a force close must not have payments
            session = sessionService.voidAllPayments(session, staffId);
        } else {
            sessionPaymentService.ensurePreAuthsAreProcessed(session);
        }

        sessionService.closeSession(session, staffId);
        asyncOrderHandlerService.onSessionClose("", masterDataService.getRestaurant(session.getRestaurantId()), session, !fullyPaid);

        if(request.isGiveBlackMark() && !fullyPaid) {
            List<String> customers = session.getDiners().stream().filter(d -> StringUtils.isNotBlank(d.getCustomerId())).map(Diner::getCustomerId).collect(Collectors.toList());
            customerService.addBlackMark(customers);
        }

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/FromParty/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postFromParty(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @PathVariable("id") String id,
                                           @NotNull @RequestBody SessionPayload sessionPayload) {
        Party party = liveDataService.getParty(id);
        if(party == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Party cannot be found");
        }

        Session session = sessionService.getSessionByPartyId(party.getId());
        if(session != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Party is already assigned to a session.");
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        if(StringUtils.isBlank(party.getRestaurantId()) || !restaurantId.equals(party.getRestaurantId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        session = sessionService.createFromParty(party, sessionPayload);
        return ResponseEntity.created(URI.create("/sessions/" + session.getId())).body(new SessionIdPojo(session.getId()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/Reopen/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putReopen(@PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(session.getClosedTime() == null) {
            return ResponseEntity.ok().build();
        }

        sessionService.updateClosed(id, null, null);
        //reinsert party if it exists
        if(StringUtils.isNotBlank(session.getOriginalPartyId())) {
            Party party = liveDataService.getParty(session.getOriginalPartyId());
            if(party == null) {
                archiveDataService.unarchiveParty(id);
            }
        }
        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Chairs/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putChairs(@PathVariable("id") String id,
                                       @NotNull @RequestBody ChairPayload chairPayload) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(StringUtils.isBlank(chairPayload.getChairData())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chair data is unavailable");

        }

        ChairData[] chairData = null;
        try {
            chairData = ControllerUtil.OBJECT_MAPPER.readValue(chairPayload.getChairData(), ChairData[].class);
            sessionService.updateChairData(id, Lists.newArrayList(chairData));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chair data is unavailable");
        }

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/PriceOffset/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putPriceOffset(@PathVariable("id") String id, @NotNull @RequestBody OffsetPayload offsetPayload) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/PercentageOffset/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putPercentageOffset(@PathVariable("id") int id, @NotNull @RequestBody OffsetPayload offsetPayload) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/AlterTip/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putAlterTip(@PathVariable("id") String id, @NotNull @RequestBody TipPayload tipPayload) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/SetTip/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putSetTip(@PathVariable("id") String id, @NotNull @RequestBody TipPayload tipPayload) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        sessionService.updateTip(id, tipPayload.getTip());

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Delay/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putDelay(@PathVariable("id") String id,
                             @NotNull @RequestBody DelayPayload delayPayload) {
        if(sessionService.exists(id) && delayPayload.getDelay() > 0) {
            timingService.delay(id, delayPayload.getDelay()*1000);
        }

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Tables/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putTables(@RequestHeader(Params.AUTHORIZATION) String token,
                                       @PathVariable("id") String id,
                                       @NotNull @RequestBody SessionPayload sessionPayload) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        if(liveDataService.tablesInUse(restaurantId, sessionPayload.getTables(), sessionPayload.getTables())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tables already in use");
        }

        if(sessionPayload.getTables().size() == 0 && session.getSessionType() == SessionType.SEATED) {
            //convert to table to tab
            session.setSessionType(SessionType.TAB);
            session.getTables().clear();
            //revive the party
            Party party = null;
            if(session.getOriginalPartyId() != null) {
                party = liveDataService.getParty(session.getOriginalPartyId());
            }
            if(party == null) {
                party = sessionService.unclearParty(session.getId());
            }
            if(party == null) {
                party = new Party();
                party.setRestaurantId(restaurantId);
                if(session.getOriginalBookingId() != null) {
                    party.setPartyType(PartyType.RESERVATION);
                    party.setBookingId(session.getOriginalBookingId());
                } else {
                    party.setPartyType(PartyType.WALK_IN);
                }
                party.setName(session.getName());
                party.setNumberOfPeople(session.getDiners().size()-1);
                party.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
                party = liveDataService.upsert(party);
                session.setOriginalParty(party);
            }
            sessionService.upsert(session);
        } else {
            //convert tab to table OR move tables
            List<Table> tables = masterDataService.getTables(restaurantId, sessionPayload.getTables());
            if(tables.size() == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tables not found");
            }

            // if this is converting a tab to a tabled session, get rid of the session
            if(session.getSessionType() == SessionType.TAB) {
                session.setSessionType(SessionType.SEATED);
                sessionService.clear(session, false, false, false, true, false);
                //apply tip if required
                Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
                sessionService.checkTipPercentage(session, restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity())));
                sessionService.upsert(session);
            }

            sessionService.updateTables(id, tables.stream().map(Table::getId).collect(Collectors.toList()));
        }
        return ResponseEntity.created(URI.create("/Sessions/Tables/"+id)).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Acknowledge/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postAcknowledge(@PathVariable("id") Session id,
                                             @NotNull @RequestBody HostAcknowledgementView acknowledgement) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/AcknowledgeAdhoc/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postAcknowledgeAdhoc(@PathVariable("id") String id,
                                                  @NotNull @RequestBody HostAcknowledgementView acknowledgement) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/split/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postSplitSession(@PathVariable("id") String id,
                                              @NotNull @RequestBody SessionSplitView sessionSplitView) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(session.getClosedTime() != null || session.getAdjustments().size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session is not live");
        }

        if(sessionSplitView.getSessionType() != SessionType.TAB) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session type is not supported");
        }

        List<Order> orders = liveDataService.getOrdersBySessionId(id);
        orders.removeIf(o -> (!sessionSplitView.getOrderIds().contains(o.getId())) || o.getAdjustment() != null);

        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        Session newSession = sessionService.createDefaultSession(restaurant, getNextSplitName(session));
        newSession.setSessionType(sessionSplitView.getSessionType());
        sessionService.upsert(newSession);

        String dinerId = newSession.getDiners().get(0).getId();
        for(Order order : orders) {
            order.setDinerId(dinerId);
            order.setSessionId(newSession.getId());
        }
        liveDataService.upsertOrders(orders);

        return ResponseEntity.created(URI.create(newSession.getId())).body(new IdPojoAndName(newSession.getId(), newSession.getName()));
    }

    @HostAuthRequired
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putSession(@PathVariable("id") String id,
                                        @NotNull @RequestBody HostPartyChangeRequest request) {

        Session session = sessionService.changeNumberOfDiners(id, request.getNumberOfDiners());
        if(StringUtils.isNotBlank(request.getName()) && !request.getName().equals(session.getName())) {
            sessionService.updateName(id, request.getName());
        }
        Party party = session.getOriginalParty();
        if(party != null) {
            party.setNumberOfPeople(request.getNumberOfDiners());
            if(StringUtils.isNotBlank(request.getName())) {
                party.setName(request.getName());
            }
            liveDataService.upsert(party);
        }

        return ResponseEntity.ok(sessionService.getSessionView(session));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}/courseAwaySent", method = RequestMethod.PUT)
    public ResponseEntity<?> putCourseAwaySent(@PathVariable("id") String id,
                                               @RequestBody IdPojo courseId) {
        if(StringUtils.isBlank(courseId.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if(!sessionService.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        sessionService.incrementCourseAway(id, courseId.getId());
        return ResponseEntity.ok().build();
    }

    private String getNextSplitName(Session sessionToSplit) {
        List<Session> current = sessionService.getLiveSessions(sessionToSplit.getRestaurantId());
        List<String> splits = current.stream().filter(s -> s.getName() != null && s.getName().contains("Split") && s.getName().contains(sessionToSplit.getName())).map(Session::getName).collect(Collectors.toList());
        if(splits.size() == 0) {
            return sessionToSplit.getName() + " [Split 1]";
        } else {
            int max = 1;
            for(String name : splits) {
                String number = name.split("Split")[1];
                number = number.substring(0, number.length()-1);
                try {
                    int n = Integer.parseInt(number.trim());
                    if(n > max) {
                        max = n;
                    }
                } catch (Exception ex) {
                    //do nothing
                }
            }
            return sessionToSplit.getName() + " [Split " + (max+1) + "]";
        }
    }
}
