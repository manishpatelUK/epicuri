package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Ignore;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.client.endpoints.MenuSetupBaseIT;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.AbsoluteBlackout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.service.util.OpeningHoursUtil;

import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@Ignore
public class TakeawayControllersTestsBase extends MenuSetupBaseIT {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        adjustmentType1.setName(StripeConstants.STRIPE_PAYMENT_TYPE);
        adjustmentType1.setSupportsChange(false);
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType1 = adjustmentTypeRepository.save(adjustmentType1);

        restaurant1.setIANATimezone("Europe/London");
        restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC)).findFirst().orElse(null).setValue(1000D);
        restaurant1.getAdjustmentTypes().add(adjustmentType1.getId());
        restaurantRepository.save(restaurant1);

        //clear opening hours
        OpeningHours openingHours = OpeningHoursUtil.createDefaultOpeningHoursOpenAllDay(BookingType.TAKEAWAY, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(openingHours);
    }

    protected CustomerTakeawayOrderRequest getCustomerTakeawayOrderRequest(long bookingTime) {
        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setRequestedTime((bookingTime / 1000) + (60 * 60));
        return setupOrderRequest(request);
    }

    protected CustomerTakeawayOrderRequest getCustomerTakeawayOrderRequest(String bookingTimeSlot) {
        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setTimeSlot(bookingTimeSlot);
        return setupOrderRequest(request);
    }

    private CustomerTakeawayOrderRequest setupOrderRequest(CustomerTakeawayOrderRequest request) {
        request.setRestaurantId(restaurant1.getId());
        request.setDelivery(false);
        request.setInstantiatedFromId(ActivityInstantiationConstant.ANDROID.getId());
        request.setNotes("foo");
        request.setTelephone("123456678");
        request.setChargeId("aChargeId");
        CustomerOrderItemView customerOrderItemView = new CustomerOrderItemView();
        customerOrderItemView.setMenuItemId(menuItem1.getId());
        customerOrderItemView.setQuantity(50);
        customerOrderItemView.setNote("bar");
        customerOrderItemView.setInstantiatedFromId(ActivityInstantiationConstant.ANDROID.getId());
        customerOrderItemView.setModifiers(Collections.singletonList(modifier1.getId()));
        request.setItems(Collections.singletonList(customerOrderItemView));
        request.setPayByCC(false);
        return request;
    }

    protected String getToken() {
        return getTokenForCustomer(customerLogin);
    }

    protected void addBlackout(long now) {
        OpeningHours openingHours = masterDataService.getOpeningHours(restaurant1.getId(), BookingType.TAKEAWAY);
        AbsoluteBlackout absoluteBlackout = new AbsoluteBlackout();
        absoluteBlackout.setStart(now);
        absoluteBlackout.setEnd(now + (1000 * 60 * 60 * 24));
        openingHours.getHours().clear();
        openingHours.getAbsoluteBlackouts().add(absoluteBlackout);
        masterDataService.upsert(openingHours);
    }

    protected Response putTakeawayCheck(CustomerTakeawayOrderRequest request, String token) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("/Order/TakeawayCheck");
    }

    protected Response putTakeawayCheckOnlineOrders(CustomerTakeawayOrderRequest request, String token) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("/onlineorders/takeaway");

    }

    protected Response postTakeaway(CustomerTakeawayOrderRequest request, String token, HttpStatus expectedStatus) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("/Order/Takeaway");
        assertEquals(expectedStatus.value(), response.getStatusCode());
        return response;
    }

    protected Response postTakeawayOnlineOrders(CustomerTakeawayOrderRequest request, String token, HttpStatus expectedStatus) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("/onlineorders/takeaway");
        assertEquals(expectedStatus.value(), response.getStatusCode());
        return response;
    }
}
