package uk.co.epicuri.serverapi.host.schedules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.service.AsyncOrderHandlerService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionService;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by manish on 03/08/2017.
 */
@Component
public class PaymentSenseJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentSenseJob.class);

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MasterDataService masterDataService;

    @Value("${epicuri.paymentsense.pull.enabled}")
    private boolean paymentSensePullEnabled;

    @Scheduled(fixedDelay = 10000)
    public void checkPayments() {
        List<Restaurant> restaurantList = masterDataService.getRestaurants();
        restaurantList.forEach(this::checkPayments);
    }

    private void checkPayments(Restaurant restaurant) {
        if(restaurant.getDeleted() != null) {
            return;
        }

        Map<ExternalIntegration, KVData> integrations = restaurant.getIntegrations();
        if(integrations.get(ExternalIntegration.PAYMENT_SENSE) == null || !paymentSensePullEnabled) {
            return;
        }

        boolean allowPayAtTable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.PS_ALLOW_PAY_AT_TABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, false)).getValue();
        if(!allowPayAtTable) {
            return;
        }

        List<Session> sessions = sessionService.getLiveSessions(restaurant.getId()).stream().filter(s -> s.getSessionType() == SessionType.SEATED || s.getSessionType() == SessionType.TAB).collect(Collectors.toList());
        LOGGER.trace("Check {} sessions for restaurant {}", sessions.size(), restaurant.getId());
        sessions.forEach( s -> {
            asyncOrderHandlerService.onReconciliationRequest("epicuriadmin", restaurant, s);
        });
    }
}
