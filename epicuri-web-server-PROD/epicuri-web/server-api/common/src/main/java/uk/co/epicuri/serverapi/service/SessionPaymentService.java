package uk.co.epicuri.serverapi.service;

import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.repository.SessionRepository;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SessionPaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionPaymentService.class);

    @Autowired
    private StripeService stripeService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    public List<Adjustment> processPayment(Session session, CreditCardData creditCardData, boolean capture) {
        return processPayment(session, creditCardData, sessionCalculationService.calculateValues(session).get(CalculationKey.TOTAL).intValue(), 0, capture);
    }

    public List<Adjustment> processPayment(Session session, CreditCardData creditCardData, int total, int gratuity, boolean capture) {
        return processPayment(session, creditCardData, total, gratuity, capture, true);
    }

    public List<Adjustment> processPayment(Session session, CreditCardData creditCardData, int total, int gratuity, boolean capture, boolean isCustomer) {
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        AdjustmentType adjustmentPaymentType = getPaymentAdjustmentType(restaurant);
        AdjustmentType adjustmentGratuityType = getGratuityAdjustmentType(restaurant);
        return processPayment(session, creditCardData, total, gratuity, restaurant, adjustmentPaymentType, adjustmentGratuityType, capture, isCustomer);
    }

    public List<Adjustment> processPayment(Session session, CreditCardData creditCardData, int total, int gratuity, Restaurant restaurant, AdjustmentType adjustmentPaymentType, AdjustmentType adjustmentGratuityType, boolean capture) {
        return processPayment(session, creditCardData, total, gratuity, restaurant, adjustmentPaymentType, adjustmentGratuityType, capture, true);
    }

    public List<Adjustment> processPayment(Session session, CreditCardData creditCardData, int total, int gratuity, Restaurant restaurant, AdjustmentType adjustmentPaymentType, AdjustmentType adjustmentGratuityType, boolean capture, boolean isCustomer) {
        List<Adjustment> adjustments = new ArrayList<>();
        if(adjustmentPaymentType == null) {
            LOGGER.warn("Cannot take payments for restaurant {} because expected payment types do not exist");
            return adjustments;
        }

        Charge charge;
        try {
            charge = stripeService.charge(restaurant, creditCardData, total+gratuity, capture, isCustomer);
        } catch (Exception e) {
            LOGGER.warn("Error in processing card payment for restaurant {}: {}", restaurant.getId(), e.getMessage());
            return adjustments;
        }

        if(charge == null || charge.getPaid() == null || !charge.getPaid()) {
            LOGGER.warn("Invalid charge object {}", charge);
            return adjustments;
        }

        ChargeSummary chargeSummary = StripeService.createSummary(charge);

        if(total > 0) { //could have been just a tip payment
            Adjustment mainAdjustment = new Adjustment(session.getId());
            mainAdjustment.setAdjustmentType(adjustmentPaymentType);
            mainAdjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
            mainAdjustment.setValue(adjustmentGratuityType == null ? total + gratuity : total);
            mainAdjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, chargeSummary);
            mainAdjustment.setCreated(System.currentTimeMillis());
            adjustments.add(mainAdjustment);
        }

        if(adjustmentGratuityType != null && gratuity > 0) {
            Adjustment gratuityAdjustment = new Adjustment(session.getId());
            gratuityAdjustment.setAdjustmentType(adjustmentGratuityType);
            gratuityAdjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
            gratuityAdjustment.setValue(gratuity);
            gratuityAdjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, chargeSummary);
            gratuityAdjustment.setCreated(System.currentTimeMillis());
            adjustments.add(gratuityAdjustment);
        }

        return adjustments;
    }

    public List<Adjustment> createAdjustmentsFromCharge(Session session, ChargeSummary chargeSummary) {
        List<Adjustment> adjustments = new ArrayList<>();
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        AdjustmentType adjustmentPaymentType = getPaymentAdjustmentType(restaurant);
        if(adjustmentPaymentType == null) {
            return adjustments;
        }

        Adjustment mainAdjustment = new Adjustment(session.getId());
        mainAdjustment.setAdjustmentType(adjustmentPaymentType);
        mainAdjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        mainAdjustment.setValue(chargeSummary.getAmount().intValue());
        mainAdjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, chargeSummary);
        mainAdjustment.setCreated(System.currentTimeMillis());
        adjustments.add(mainAdjustment);

        return adjustments;
    }

    public boolean processAcquisition(Customer customer, CreditCardData ccData) {
        if(customer == null || ccData == null || ccData.getCcToken() == null) {
            return false;
        }

        try {
            com.stripe.model.Customer stripeCustomer = stripeService.acquireCustomer(ccData.getCcToken(), customer.getEmail(), customer.getId());
            if(stripeCustomer != null) {
                ccData.setExternalId(stripeCustomer.getId());
                return true;
            } else {
                LOGGER.warn("Could not acquire Customer object for {}", customer.getId());
                return false;
            }
        } catch (StripeException e) {
            LOGGER.warn("Could not acquire Customer object for {}: {}", customer.getId(), e.getMessage());
            return false;
        }
    }

    public void ensurePreAuthsAreProcessed(Session session) {
        for(Adjustment adjustment : session.getAdjustments()) {
            if(!adjustment.isVoided() && adjustment.getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY)) {
                try {
                    stripeService.capturePayment(adjustment);
                    sessionRepository.removeAdjustment(session.getId(), adjustment.getId());
                    sessionRepository.pushAdjustment(session.getId(), adjustment);
                } catch (StripeException e) {
                    LOGGER.warn("Error trying to capture charge: " + e.getMessage());

                    //send an internal email to the restaurant
                    Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
                    sendPaymentFailedEmail(session, adjustment, restaurant);
                }
            }
        }
    }

    private void sendPaymentFailedEmail(Session session, Adjustment adjustment, Restaurant restaurant) {
        String email = StringUtils.isBlank(restaurant.getInternalEmailAddress()) ? restaurant.getPublicEmailAddress() : restaurant.getInternalEmailAddress();
        if(StringUtils.isBlank(email)) {
            return;
        }

        String subject = "ALERT: Failed payment on Session: " + session.getReadableId() + " at " + TimeUtil.getRestaurantTime(session.getStartTime(), restaurant.getIANATimezone());
        StringBuilder body = new StringBuilder();
        String newLine = "\r\n";
        body.append("There was an attempted payment by credit Card by a guest which has failed (payment from Guest App via Stripe). Please reconcile manually.").append(newLine);
        body.append("Session ID: ").append(session.getReadableId()).append(newLine);
        body.append("Session Type: ").append(session.getSessionType());
        if(session.getSessionType() == SessionType.TAKEAWAY) {
            body.append(" (").append(session.getTakeawayType()).append(")");
        }
        body.append(newLine);
        Booking booking = session.getOriginalBooking();
        if(booking != null) {
            body.append("Booking: ").append(booking.getName()).append(newLine);
            if(booking.getDeliveryAddress() != null) {
                body.append("Address Line 1: ").append(booking.getDeliveryAddress().getStreet()).append(newLine);
                body.append("Postal Code: ").append(booking.getDeliveryAddress().getPostcode()).append(newLine);
            }
        }
        body.append("Payment ID: ").append(adjustment.getId()).append(newLine);
        body.append("Amount: ").append(String.format("%.2f",MoneyService.toMoneyRoundNearest(adjustment.getValue()))).append(newLine);
        body.append(newLine);
        body.append(newLine);
        body.append("Payment may have been taken manually already. You can reconcile payments with the Payments Report on the portal: https://portal.epicuri.co.uk").append(newLine);

        asyncCommunicationsService.sendSimpleEmail(email, "support@epicuri.co.uk", subject, body.toString());
    }

    public void ensurePreAuthsAreCancelled(Session session) {
        for(Adjustment adjustment : session.getAdjustments()) {
            setVoidOnAuth(session, adjustment, true);
        }
    }

    public void ensurePreAuthsAreReinstated(Session session) {
        for(Adjustment adjustment : session.getAdjustments()) {
            setVoidOnAuth(session, adjustment, false);
        }
    }

    private void setVoidOnAuth(Session session, Adjustment adjustment, boolean voidValue) {
        if(!adjustment.isVoided() && adjustment.getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY)) {
            try {
                if(stripeService.hasPreAuth(adjustment)) {
                    adjustment.setVoided(voidValue);
                    sessionRepository.removeAdjustment(session.getId(), adjustment.getId());
                    sessionRepository.pushAdjustment(session.getId(), adjustment);
                }
            } catch (StripeException e) {
                LOGGER.warn("Error trying to cancel charge: " + e.getMessage());
            }
        }
    }

    private AdjustmentType getPaymentAdjustmentType(Restaurant restaurant) {
        List<AdjustmentType> adjustmentTypes = masterDataService.getAdjustmentTypes(restaurant.getAdjustmentTypes());
        return adjustmentTypes.stream().filter(a -> a.getName().equals(StripeConstants.STRIPE_PAYMENT_TYPE)).findFirst().orElse(null);
    }

    private AdjustmentType getGratuityAdjustmentType(Restaurant restaurant) {
        List<AdjustmentType> adjustmentTypes = masterDataService.getAdjustmentTypes(restaurant.getAdjustmentTypes());
        return adjustmentTypes.stream().filter(a -> a.getName().equals(StripeConstants.STRIPE_GRATUITY_TYPE)).findFirst().orElse(null);
    }
}
