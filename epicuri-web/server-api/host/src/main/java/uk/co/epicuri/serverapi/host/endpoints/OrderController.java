package uk.co.epicuri.serverapi.host.endpoints;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.OrderAttributionView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BatchService batchService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postOrder(@RequestHeader(Params.AUTHORIZATION) String token,
                                       @NotNull @RequestBody List<OrderRequest> requests,
                                       @RequestParam(value = "orderPrintsRequired", defaultValue = "true", required = false) boolean orderPrintsRequired) {
        String restaurantId = authenticationService.getRestaurantId(token);
        requests = requests.stream().filter
                (r -> StringUtils.isNotBlank(r.getDinerId())
                || StringUtils.isNotBlank(r.getSessionId())).collect(Collectors.toList());

        Set<String> sessionIds = new HashSet<>();
        for(OrderRequest request : requests) {
            if(StringUtils.isNotBlank(request.getSessionId())) {
                sessionIds.add(request.getSessionId());
            } else if(StringUtils.isNotBlank(request.getDinerId())) {
                sessionIds.add(IDAble.extractParentId(request.getDinerId()));
            }
        }

        List<Order> orders = liveDataService.insertOrders(restaurantId, requests, authenticationService.getStaffId(token), orderPrintsRequired);
        List<Session> sessions = sessionService.getSessions(sessionIds);
        List<Booking> bookings = batchService.getBookings(sessions);

        List<HostBatchView> list = new ArrayList<>();
        if(orderPrintsRequired) {
            list = batchService.getHostBatchViews(restaurantId,
                    sessions,
                    bookings.stream().collect(Collectors.toMap(Booking::getId, Function.identity())),
                    orders.stream().collect(Collectors.groupingBy(Order::getSessionId)),
                    true);
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setBatches(list);

        return ResponseEntity.created(URI.create("Order")).body(orderResponse);
    }

    @HostAuthRequired
    @RequestMapping(value = "/RemoveOrderFromBill/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> removeOrderFromBill(@RequestHeader(Params.AUTHORIZATION) String token,
                                                 @PathVariable("id") String id,
                                                 @NotNull @RequestBody OrderRemovalAdjustmentRequest request) {
        if(StringUtils.isBlank(request.getAdjustmentTypeId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        AdjustmentType requestedAdjustmentType = masterDataService.getAdjustmentType(request.getAdjustmentTypeId());
        if(requestedAdjustmentType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adjustment type not found");
        }

        Order order = liveDataService.getOrder(id);
        if(order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        if(order.getDeleted() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Order is deleted");
        }

        Adjustment adjustment = new Adjustment(id);
        adjustment.setAdjustmentType(requestedAdjustmentType);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(order.getPriceOverride());
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setStaffId(authenticationService.getStaffId(token));
        order.setAdjustment(adjustment);

        liveDataService.upsertOrder(order);
        asyncOrderHandlerService.onOrderRemoved("",authenticationService.getRestaurantId(token),order, liveDataService.getOrders(order.getSessionId()));

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/RemoveAllOrdersFromSession/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> removeAllOrdersFromSession(@RequestHeader(Params.AUTHORIZATION) String token,
                                                        @PathVariable("id") String id) {
        List<Order> orders = liveDataService.getOrdersBySessionId(id);
        if(orders == null || orders.size() == 0) {
            return ResponseEntity.ok().build();
        }

        liveDataService.cancelBatchByOrder(orders.stream().map(Order::getId).collect(Collectors.toList()));
        liveDataService.deleteOrders(orders);

        asyncOrderHandlerService.onAllOrdersRemoved("", authenticationService.getRestaurantId(token), id, orders);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/allocate/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> reallocateOrders(@PathVariable("id") String id,
                                              @RequestBody @NotNull OrderAttributionView orderAttributionView) {
        if(StringUtils.isBlank(orderAttributionView.getDinerId())) {
            return ResponseEntity.badRequest().build();
        }

        List<Order> orders = liveDataService.getOrdersBySessionId(id);
        if(orders == null || orders.size() == 0) {
            return ResponseEntity.ok().build();
        }

        Map<String,Order> orderMap = orders.stream().collect(Collectors.toMap(Order::getId, Function.identity()));

        List<Order> updated = new ArrayList<>();
        for(String orderId : orderAttributionView.getOrderIds()) {
            if(!orderMap.containsKey(orderId)) {
                continue;
            }

            Order order = orderMap.get(orderId);
            order.setDinerId(orderAttributionView.getDinerId());
            updated.add(order);
        }

        if(orderAttributionView.getUnassignedOrderIds().size() > 0) {
            Session session = sessionService.getSession(id);
            Diner tableDiner = session.getDiners().stream().filter(Diner::isDefaultDiner).findFirst().orElse(null);
            if(tableDiner != null) {
                for (String orderId : orderAttributionView.getUnassignedOrderIds()) {
                    if(!orderMap.containsKey(orderId) || orderAttributionView.getOrderIds().contains(orderId)) {
                        continue;
                    }

                    Order order = orderMap.get(orderId);
                    order.setDinerId(tableDiner.getId());
                    updated.add(order);
                }
            }
        }

        liveDataService.upsertOrders(updated);

        return ResponseEntity.ok().build();
    }
}
