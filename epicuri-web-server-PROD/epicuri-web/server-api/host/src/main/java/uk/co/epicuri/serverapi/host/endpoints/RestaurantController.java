package uk.co.epicuri.serverapi.host.endpoints;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.auth.HostLevelCheckRequired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.TerminalList;
import uk.co.epicuri.serverapi.common.pojo.host.ClosureDatePair;
import uk.co.epicuri.serverapi.common.pojo.host.HostClosuresView;
import uk.co.epicuri.serverapi.common.pojo.host.HostOpeningHoursView;
import uk.co.epicuri.serverapi.common.pojo.host.HostRestaurantView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.StaffPermissionsService;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;
import uk.co.epicuri.serverapi.service.util.OpeningHoursUtil;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Restaurant", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class RestaurantController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private StaffPermissionsService staffPermissionsService;

    @Value("${epicuri.url}")
    private String apiURL;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getRestaurant(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Restaurant not found");
        }

        AdjustmentType mewsAdjustmentType = null;
        if(restaurant.getIntegrations().containsKey(ExternalIntegration.MEWS)
                && restaurant.getIntegrations().get(ExternalIntegration.MEWS).getToken() != null) {
            mewsAdjustmentType = masterDataService.getAdjustmentTypeByName(MewsConstants.MEWS_ADJUSTMENT_TYPE);
        }

        Map<String,AdjustmentType> adjustmentTypeMap = masterDataService.getAdjustmentTypes(restaurant.getAdjustmentTypes())
                .stream().collect(Collectors.toMap(AdjustmentType::getId, Function.identity()));

        if(mewsAdjustmentType != null) {
            adjustmentTypeMap.put(mewsAdjustmentType.getId(), mewsAdjustmentType);
        }

        Staff staff = masterDataService.getStaff(authenticationService.getStaffId(token));
        StaffPermissions staffPermissions = staffPermissionsService.getPermissions(restaurant);
        Map<WaiterAppFeature,Boolean> permissions = StaffPermissionsService.getIndividualPermissions(staffPermissions, staff.getRole());

        HostRestaurantView restaurantView = new HostRestaurantView(restaurant, adjustmentTypeMap, apiURL, permissions);

        List<RestaurantDefault> defaults = restaurant.getRestaurantDefaults();
        defaults.forEach(d -> restaurantView.getRestaurantDefaults().put(d.getName(), d.getValue()));

        return ResponseEntity.ok(restaurantView);
    }

    @RequestMapping(value = "/BillLogo/{id:.+}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getLogoImage(@PathVariable("id")String id,
                                               HttpServletResponse response) {
        if(id.contains(".")) {
            id = id.substring(0, id.lastIndexOf('.'));
        }

        RestaurantImage image = masterDataService.getRestaurantImage(id);
        if(image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
        }
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        return ResponseEntity.ok(image.getImage());
    }

    @HostAuthRequired
    @RequestMapping(value = "/OpeningHours", method = RequestMethod.GET)
    public ResponseEntity<?> getOpeningHours(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestParam(defaultValue = "RESERVATION", value = "type") String type) {
        String restaurantId = authenticationService.getRestaurantId(token);
        OpeningHours openingHours = getOpeningHoursAndUpsertIfMissing(type, restaurantId);
        return ResponseEntity.ok(new HostOpeningHoursView(openingHours));
    }

    @HostAuthRequired
    @RequestMapping(value = "/OpeningHours", method = RequestMethod.PUT)
    public ResponseEntity<?> putOpeningHours(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestParam(defaultValue = "RESERVATION", value = "type") String type,
                                             @RequestBody HostOpeningHoursView openingHoursView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        OpeningHours openingHours = masterDataService.getOpeningHours(restaurantId, BookingType.valueOf(type));

        if(openingHours == null) {
            openingHours = OpeningHoursUtil.createDefaultOpeningHoursClosedAllDay(BookingType.valueOf(type), restaurantId);
        }

        Map<DayOfWeek, List<HourSpan>> hours = openingHoursView.getHours();
        clean(hours);
        openingHours.setHours(hours);
        masterDataService.upsert(openingHours);

        return ResponseEntity.ok().build();
    }

    private void clean(Map<DayOfWeek, List<HourSpan>> hours) {
        for(Map.Entry<DayOfWeek,List<HourSpan>> entry : hours.entrySet()){
            boolean openAllDayPresent = false;
            final List<HourSpan> spans = entry.getValue();
            for(HourSpan span : spans){
                //if h = 24, min should be 0
                if(span.getHourClose() == 24) {
                    span.setMinuteClose(0);

                    //if opens at 00:00 as well, then it's an all day open
                    if(span.getHourOpen() == 0 && span.getMinuteOpen() == 0) {
                        openAllDayPresent = true;
                    }
                }
            }

            // only allow 1 entry when open all day
            if(openAllDayPresent && spans.size() > 1) {
                hours.put(entry.getKey(),OpeningHours.getDefaultOpenAllDaysHours());
            }

            //ensure no overlap
            if(spans.size() >= 2 ) {
                hours.values().forEach(Collections::sort);
                for (int i = 0; i < spans.size() - 1; i++) {
                    HourSpan span1 = spans.get(0);
                    HourSpan span2 = spans.get(1);
                    if(OpeningHoursUtil.isOverlapping(span1, span2)) {
                        OpeningHoursUtil.joinAdjacent(span1,span2);
                    }
                }
            }

            hours.values().forEach(Collections::sort);
        }
    }

    @HostAuthRequired
    @RequestMapping(value = "/AbsoluteClosures", method = RequestMethod.GET)
    public ResponseEntity<?> getClosures(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestParam(defaultValue = "RESERVATION", value = "type") String type,
                                         @RequestParam(defaultValue = "false", value = "archived") boolean archived) {
        String restaurantId = authenticationService.getRestaurantId(token);
        OpeningHours openingHours = getOpeningHoursAndUpsertIfMissing(type, restaurantId);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        cleanHistoricalBlackouts(openingHours);
        List<AbsoluteBlackout> list = new ArrayList<>();
        if(!archived) {
            list.addAll(openingHours.getAbsoluteBlackouts());
        } else {
            list.addAll(openingHours.getHistoricalBlackouts());
            list.addAll(openingHours.getAbsoluteBlackouts());
        }

        return ResponseEntity.ok(new HostClosuresView(restaurant.getIANATimezone(), list));
    }

    private OpeningHours getOpeningHoursAndUpsertIfMissing(@RequestParam(defaultValue = "RESERVATION", value = "type") String type, String restaurantId) {
        OpeningHours openingHours = masterDataService.getOpeningHours(restaurantId, BookingType.valueOf(type));
        if (openingHours == null) {
            openingHours = OpeningHoursUtil.createDefaultOpeningHoursClosedAllDay(BookingType.valueOf(type), restaurantId);
            masterDataService.upsert(openingHours);
        }
        return openingHours;
    }

    private void cleanHistoricalBlackouts(OpeningHours openingHours) {
        long cutOff = System.currentTimeMillis() - (1000*60*60*24*2);
        Iterator<AbsoluteBlackout> iterator = openingHours.getAbsoluteBlackouts().iterator();
        boolean hasChanged = false;
        while(iterator.hasNext()) {
            AbsoluteBlackout absoluteBlackout = iterator.next();
            if (absoluteBlackout.getEnd() < cutOff) {
                iterator.remove();
                openingHours.getHistoricalBlackouts().add(absoluteBlackout);
                hasChanged = true;
            }
        }

        if(hasChanged) {
            masterDataService.upsert(openingHours);
        }
    }

    @HostAuthRequired
    @RequestMapping(value = "/AbsoluteClosures", method = RequestMethod.PUT)
    public ResponseEntity<?> putClosures(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestParam(defaultValue = "RESERVATION", value = "type") String type,
                                         @RequestBody HostClosuresView hostClosuresView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        OpeningHours openingHours = getOpeningHoursAndUpsertIfMissing(type, restaurantId);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        openingHours.getAbsoluteBlackouts().clear();
        for(ClosureDatePair closureDatePair : hostClosuresView.getClosures()) {
            long start = HostClosuresView.fromDateString(restaurant.getIANATimezone(), closureDatePair.getStart());
            long end = HostClosuresView.fromDateString(restaurant.getIANATimezone(), closureDatePair.getEnd());
            AbsoluteBlackout absoluteBlackout = new AbsoluteBlackout();
            absoluteBlackout.setStart(start);
            absoluteBlackout.setEnd(end);
            openingHours.getAbsoluteBlackouts().add(absoluteBlackout);
        }

        masterDataService.upsert(openingHours);

        return ResponseEntity.ok().build();
    }
}
