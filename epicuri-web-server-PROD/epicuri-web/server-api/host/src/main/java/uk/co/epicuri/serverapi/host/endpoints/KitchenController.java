package uk.co.epicuri.serverapi.host.endpoints;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.service.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Kitchen")
public class KitchenController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenController.class);

    @Value("${epicuri.portal}")
    private String portalURL;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private OrderScheduleService orderScheduleService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public RedirectView getIndex(RedirectAttributes attributes,
                         @RequestParam("Auth") String token,
                         @RequestParam("Printer") String printerId) {
        attributes.addAttribute("Auth", token);
        attributes.addAttribute("Printer", printerId);
        return new RedirectView(portalURL);
    }

    @HostAuthRequired
    @RequestMapping(value = "/Done", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMarkDone(@RequestBody MarkItemDoneRequest markItemDoneRequest) {
        return markItems(markItemDoneRequest, true);
    }

    @HostAuthRequired
    @RequestMapping(value = "/Undone", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putUnDone(@RequestBody MarkItemDoneRequest markItemDoneRequest) {
        return markItems(markItemDoneRequest, false);
    }

    public ResponseEntity<?> markItems(MarkItemDoneRequest markItemDoneRequest, boolean done) {
        if(markItemDoneRequest.getOrderId().contains(",")) {
            String[] ids = StringUtils.split(markItemDoneRequest.getOrderId(), ",");
            for(String id : ids) {
                //ignore errors for now
                if(StringUtils.isBlank(id)) {
                    continue;
                }
                markItemDoneRequest.setOrderId(id);
                markItemDone(markItemDoneRequest, done);
            }
        } else {
            return markItemDone(markItemDoneRequest, done);
        }

        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> markItemDone(MarkItemDoneRequest markItemDoneRequest, boolean asDone) {
        if(StringUtils.isBlank(markItemDoneRequest.getOrderId())) {
            return ResponseEntity.badRequest().body("Order ID not present");
        }

        Order order = liveDataService.getOrder(markItemDoneRequest.getOrderId());
        if(order == null) {
            return ResponseEntity.notFound().build();
        }

        markDoneField(asDone, System.currentTimeMillis(), order);
        liveDataService.upsertOrder(order);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/AllDone", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMarkAllDone(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @RequestBody MarkAllItemsDoneRequest markAllItemsDoneRequest) {
        return markAllAsDone(token, markAllItemsDoneRequest, true);
    }

    @HostAuthRequired
    @RequestMapping(value = "/AllUndone", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putAllUndone(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @RequestBody MarkAllItemsDoneRequest markAllItemsDoneRequest) {
        return markAllAsDone(token, markAllItemsDoneRequest, false);
    }

    private ResponseEntity<?> markAllAsDone(String token, MarkAllItemsDoneRequest markAllItemsDoneRequest, boolean asDone) {
        if(StringUtils.isBlank(markAllItemsDoneRequest.getSessionId())) {
            return ResponseEntity.badRequest().body("Session ID not present");
        }

        Session session = sessionService.getSession(markAllItemsDoneRequest.getSessionId());
        if(session == null || !authenticationService.getRestaurantId(token).equals(session.getRestaurantId())) {
            return ResponseEntity.notFound().build();
        }

        List<Order> orders = liveDataService.getOrders(session.getId());
        Map<String,Batch> batches = new HashMap<>();
        if(markAllItemsDoneRequest.getBatchId() != null) {
            batches = liveDataService.getBatchesBySessionId(session.getId()).stream().collect(Collectors.toMap(Batch::getId, Function.identity()));
        }
        long time = System.currentTimeMillis();
        final Map<String,Batch> finalBatches = batches;
        orders.forEach(o -> {
            if(markAllItemsDoneRequest.getPrinterId() == null || session.getSessionType() == SessionType.TAKEAWAY) {
                markDoneField(asDone, time, o);
            } else {
                Batch batch = finalBatches.get(markAllItemsDoneRequest.getBatchId());
                markItemsOnPrinterDone(markAllItemsDoneRequest, asDone, time, o, batch);
            }
        });
        liveDataService.upsertOrders(orders);

        return ResponseEntity.ok().build();
    }

    private void markItemsOnPrinterDone(MarkAllItemsDoneRequest markAllItemsDoneRequest, boolean asDone, long time, Order o, Batch batch) {
        if(batch != null && !batch.getOrderIds().contains(o.getId())) {
            return;
        }
        MenuItem menuItem = o.getMenuItem();
        if (menuItem == null) {
            menuItem = masterDataService.getItem(o.getMenuItemId());
        }
        if (menuItem.getDefaultPrinter().equals(markAllItemsDoneRequest.getPrinterId())) {
            markDoneField(asDone, time, o);
        }
    }

    private void markDoneField(boolean asDone, long time, Order o) {
        if(asDone) {
            o.setDoneTime(time);
        } else {
            o.setDoneTime(null);
        }
    }

    @HostAuthRequired
    @RequestMapping(value = "/Orders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PrinterTicketsResponse>> getOrders(@RequestHeader(Params.AUTHORIZATION) String token,
                                                                  @RequestParam(value = "printerId", required = false) String printerId,
                                                                  @RequestParam(value = "aggregateBySession", required = false, defaultValue = "false") boolean aggregateBySession) {
        String restaurantId = authenticationService.getRestaurantId(token);
        return ResponseEntity.ok(orderScheduleService.getScheduleList(restaurantId, printerId, aggregateBySession));
    }
}
