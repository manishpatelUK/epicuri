package uk.co.epicuri.bookingapi.endpoints.aurusit;


import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QException;
import com.exxeleron.qjava.QTable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Service;
import uk.co.epicuri.api.core.pojo.order.OrderItemRequest;
import uk.co.epicuri.api.core.pojo.session.*;
import uk.co.epicuri.bookingapi.endpoints.auth.AbstractSecurityConnectingResource;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrder;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrderRequest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("aurusit/{id}/order")
public class AurusitController extends AbstractSecurityConnectingResource {

    private final EpicuriAPI epicuriAPI;
    private final QConnection aurusitConnection;

    private final Map<Integer,Integer> defaultCourses = new HashMap<>();
    private final Map<Integer,List<Service>> services = new HashMap<>();

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    public AurusitController(EpicuriAPI epicuriAPI, QConnection securityConnection, QConnection aurusitConnection) {
        super(securityConnection);
        this.epicuriAPI = epicuriAPI;
        this.aurusitConnection = aurusitConnection;
    }

    @Path("/table")
    @PUT
    public Response orderOnTable(@PathParam("id") int restaurantId, @HeaderParam("X-Auth-Token") String token, @NotNull AurusitOrderRequest orders) {
        System.out.println(orders.toString());
        QTable sessionInfo = getSessionInfo(restaurantId);
        if(determineTableExists(restaurantId, orders.getTableName(), sessionInfo)) {
            return insertOrders(restaurantId, token, orders, true, sessionInfo);
        } else {
            orders.setAdhocName(orders.getTableName());
            return insertOrders(restaurantId,token,orders,false,sessionInfo); // tab
        }
    }

    @Path("/adhoc")
    @PUT
    public Response orderOnTab(@PathParam("id") int restaurantId, @HeaderParam("X-Auth-Token") String token, @NotNull AurusitOrderRequest orders) {
        return insertOrders(restaurantId,token,orders,false,getSessionInfo(restaurantId));
    }

    public boolean determineTableExists(@PathParam("id") int restaurantId, String tableName, QTable sessionInfo) {
        String[] tableNames = (String[])sessionInfo.getData()[sessionInfo.getColumnIndex("tableName")];
        return ArrayUtils.contains(tableNames, tableName);
    }

    public QTable getSessionInfo(@PathParam("id") int restaurantId) {
        QTable sessionInfo = null;
        try {
            sessionInfo = (QTable)aurusitConnection.sync("getSessionInfo",restaurantId);
        } catch (QException | IOException e ) {
            throw new InternalServerErrorException("db is throwing an error: " + e.getMessage());
        }
        return sessionInfo;
    }

    private Response insertOrders(int restaurantId, String token, AurusitOrderRequest orders, boolean isTabled, QTable sessionInfo) {
        String realToken = getToken(token);
        protractExpiry(String.valueOf(restaurantId));

        if(orders.getItems() == null || orders.getItems().size() == 0) {
            return Response.ok().build();
        }

        // check if prices vs items are ok
        int[] externalIds = new int[orders.getItems().size()];
        int[] prices = new int[orders.getItems().size()];
        for(int i = 0; i < orders.getItems().size(); i++) {
            externalIds[i] = orders.getItems().get(i).getItemId();
            prices[i] = orders.getItems().get(i).getPrice();
        }

        boolean pricesOK = false;
        QTable map;
        try {
            Object[] result = (Object[]) aurusitConnection.sync("checkPricesAndGetMenuItemIds",externalIds,prices);
            pricesOK = (Boolean)result[0];
            map = (QTable)result[1];
        } catch (QException | IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }

        if(!pricesOK || map == null || map.getRowsCount() == 0) {
            throw new BadRequestException("One or more price/id combinations do not match menu items or pricing map is out of date");
        }

        // adhoc name is optional
        if(!isTabled && StringUtils.isBlank(orders.getAdhocName())) {
            orders.setAdhocName("Temp " + atomicInteger.incrementAndGet());
        }
        String name = isTabled ? orders.getTableName() : orders.getAdhocName();

        int defaultCourseId = -1;
        int defaultServiceId = -1;
        if(sessionInfo != null) {
            defaultCourseId = (Integer)sessionInfo.get(0).get(sessionInfo.getColumnIndex("courseId"));
            defaultServiceId = (Integer)sessionInfo.get(0).get(sessionInfo.getColumnIndex("serviceId"));
        }
        else {
            // get it from the main database
            defaultCourseId = getDefaultCourseId(restaurantId, realToken);
            defaultServiceId = getServices(restaurantId, realToken).get(0).getId();
        }

        Session session = getSession(restaurantId, realToken, name, isTabled, sessionInfo, defaultServiceId);

        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        for(AurusitOrder order : orders.getItems()) {
            int internalId = getInternalId(order.getItemId(), order.getPrice(), map);
            if(internalId < 0) {
                throw new InternalServerErrorException("cannot match external id and price to internal code");
            }

            OrderItemRequest request = new OrderItemRequest();
            request.setCourseId(defaultCourseId);
            request.setDinerId(session.getDiners().get(0).getId());
            request.setInstantiatedFromId(0); // default
            request.setMenuItemId(internalId);
            request.setQuantity(order.getQuantity());
            if(StringUtils.isNotBlank(order.getNote())) {
                request.setNote(order.getNote());
            }

            orderItemRequests.add(request);
        }

        epicuriAPI.addOrders(orderItemRequests,realToken);

        return Response.ok().build();
    }

    private int getDefaultCourseId(int restaurantId, String token) {
        synchronized (defaultCourses) {
            if(!defaultCourses.containsKey(restaurantId)) {
                List<Service> serviceList = getServices(restaurantId,token);
                defaultCourses.put(restaurantId, serviceList.get(0).getCourses().get(0).getId());
            }

            return defaultCourses.get(restaurantId);
        }
    }

    private List<Service> getServices(int restaurantId, String token) {
        synchronized (services) {
            if(!services.containsKey(restaurantId)) {
                services.put(restaurantId,epicuriAPI.getServices(token));
            }
        }

        return services.get(restaurantId);
    }

    private int getInternalId(int externalId, int price, QTable table) {
        for(int i = 0; i < table.getRowsCount(); i++) {
            QTable.Row row = table.get(i);
            if(((Integer)row.get(table.getColumnIndex("externalId"))) == externalId
                    && ((Integer)row.get(table.getColumnIndex("price"))) == price) {
                return (Integer)row.get(table.getColumnIndex("internalId"));
            }
        }

        return -1;
    }

    private Session getSession(int restaurantId, String token, String tableNameOrAdhocName, boolean isTabled, QTable sessionInfo, int defaultServiceId) {
        List<Integer> selectedTables = new ArrayList<>();

        SessionResponse sessionResponse = epicuriAPI.getSessions(token);
        for (Session session : sessionResponse.getSessions()) {
            if(session.getPartyName().equals(tableNameOrAdhocName) || containsTable(session.getTables(),tableNameOrAdhocName)) {
                return session;
            }
        }

        // if there's no active sessions on said table. Find the table, and create a session on it
        if(isTabled && sessionInfo == null) {
            // brute force if sessionInfo was not obtained
            List<Table> tables = epicuriAPI.getTables(token);

            for (Table table : tables) {
                if (table.getName().equals(tableNameOrAdhocName)) {
                    selectedTables.add(table.getId());
                    break;
                }
            }

            if (selectedTables.size() == 0) {
                throw new BadRequestException("requested table does not exist");
            }
        }
        else if(isTabled) { //sessionInfo != null
            for(int i = 0; i < sessionInfo.getRowsCount(); i++) {
                String name = (String)sessionInfo.get(i).get(sessionInfo.getColumnIndex("tableName"));
                if(name.equals(tableNameOrAdhocName)) {
                    selectedTables.add((Integer)sessionInfo.get(i).get(sessionInfo.getColumnIndex("tableId")));
                }
            }
        }

        NewPartyRequest request = new NewPartyRequest();
        request.setName(tableNameOrAdhocName);
        request.setNumberOfPeople(1);
        request.setCreateSession(true);
        request.setServiceId(defaultServiceId);
        request.setTables(selectedTables);

        System.out.println("New party request: " + request);
        NewPartyResponse response = isTabled ? epicuriAPI.addNewParty(request,token) : epicuriAPI.addNewUnseatedParty(request, token);
        System.out.println("New party response: " + response);

        Session session = epicuriAPI.getSession(response.getSessionId(),token);
        if(session.getDiners().size() == 0) {
            // try again
            System.out.println("No diners... try again");
            session = epicuriAPI.getSession(response.getSessionId(),token);
        }
        if(session.getDiners().size() == 0) {
            throw new BadRequestException("No diners associated with session");
        }

        return session;
    }

    private void checkClosure(Session session) {
        if(session.getClosedTime() > session.getStartTime()) {
            // reopen!

        }
    }


    private boolean containsTable(List<Table> tables, String requestedTableName) {
        for(Table table : tables) {
            if(table.getName().equals(requestedTableName)) {
                return true;
            }
        }
        return false;
    }

}
