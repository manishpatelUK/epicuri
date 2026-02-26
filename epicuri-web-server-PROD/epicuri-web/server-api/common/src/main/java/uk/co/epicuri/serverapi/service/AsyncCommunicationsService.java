package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.comms.EmailRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.service.external.EmailService;
import uk.co.epicuri.serverapi.service.external.IMailBuilder;
import uk.co.epicuri.serverapi.service.external.SMSService;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncCommunicationsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncCommunicationsService.class);

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Value("${epicuri.url}")
    private String epicuriBaseURL;

    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private BookingService bookingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    @Lazy
    private CustomerService customerService;

    @Autowired
    private LiveDataService liveDataService;

    @Async
    public CompletableFuture<Void> sendInternalEmail(String fromName, String subject, String body) {
        return sendSimpleEmail("info@epicuri.co.uk", fromName, subject, body);
    }

    @Async
    public CompletableFuture<Void> sendInternalSupportEmail(String fromName, String subject, String body) {
        return sendSimpleEmail("support@epicuri.co.uk", fromName, subject, body);
    }

    @Async
    public CompletableFuture<Void> sendSimpleEmail(String toAddress, String fromName, String subject, String body) {
        try {
            sendSimpleEmailNonAsync(toAddress, fromName, subject, body);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public boolean sendSimpleEmailNonAsync(String toAddress, String fromName, String subject, String body) {
        return emailService.simpleEmail(subject, toAddress, fromName, "info@epicuri.co.uk", body).build();
    }

    @Async
    public CompletableFuture<Void> onNewCustomer(Customer customer) {
        //if there is an email address, email them
        //if not, try and text the phone number
        //otherwise, fail
        if(StringUtils.isNotBlank(customer.getEmail()) && ControllerUtil.EMAIL_REGEX.matcher(customer.getEmail()).matches()) {
            LOGGER.info("Try sending new customer email to {} with email address", customer.getId(), customer.getEmail());
            sendRegistrationConfirmationToDiner(customer.getEmail());
            customerService.pushLegalCommunicationsSent(customer.getId());
        } else if(StringUtils.isNotBlank(customer.getInternationalCode()) && StringUtils.isNotBlank(customer.getPhoneNumber())) {
            LOGGER.info("Customer {} doesn't have an email address; try sending an sms to {} {}", customer.getId(), customer.getInternationalCode(), customer.getPhoneNumber());
            //smsService.send("Hi! Just so you know, all your data is kept securely with us and can be deleted whenever you wish! For more information please see review our terms on epicuri.co.uk/guest.html", customer.getInternationalCode() + customer.getPhoneNumber());
            //customerService.pushLegalCommunicationsSent(customer.getId());
        } else {
            LOGGER.info("Could not contact customer {} to conform with our legal requirements on phone or email", customer.getId());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendSMSCode(String code, String internationalCode, String number, String email) {
        LOGGER.debug("Sending SMS to {} {}", internationalCode, number);
        boolean sent = smsService.send("Your security code is: " + code + ". Thanks for signing up!", internationalCode + number);
        if(!sent) {
            LOGGER.error("Could not send SMS");
        }

        if(!sent && email != null && ControllerUtil.EMAIL_REGEX.matcher(email).matches()) {
            emailService.simpleEmail("Your Epicuri Security Code for number: " + internationalCode + number,
                    email,
                    "Epicuri",
                    "no-reply@epicuri.email",
                    "Hey there! We couldn't send a security code to your phone :( Maybe it's us, maybe it's you... but please try again and double-check the number! Contact us at support@epicuri.co.uk if you feel like the universe is ganging up on you. Thanks!");
        } else if(!sent) {
            LOGGER.warn("Could not send security code to {} / {}", number, email);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendBookingConfirmations(String bookingId, String restaurantId) {
        Booking booking = bookingService.getBooking(bookingId);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        try {
            sendEmailToDiner(booking, restaurant);
        } catch (Exception ex){LOGGER.error("Could not send email to diner", ex);}
        try {
            sendEmailToRestaurant(booking, restaurant);
        } catch (Exception ex){LOGGER.error("Could not send email to restaurant", ex);}

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendReceiptToCustomer(Session session, List<EmailRequest> emailRequests) {
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        List<Order> orders = liveDataService.findOrders(session);
        Map<CalculationKey,Number> calculations = sessionCalculationService.calculateValues(session, orders);
        try {
            return sendReceiptToCustomer(session, emailRequests, restaurant, orders, calculations);
        } catch (Exception ex) {
            LOGGER.error("Error sending billing email", ex);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async
    public CompletableFuture<Void> sendCashup(CashUp cashUp) {
        Restaurant restaurant = masterDataService.getRestaurant(cashUp.getRestaurantId());
        sendCashup(cashUp, restaurant);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> sendReceiptToCustomer(Session session, List<EmailRequest> emailRequests, Restaurant restaurant, List<Order> orders, Map<CalculationKey, Number> calculations) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(restaurant.getISOCurrency()));

        Map<String,String> replacements = new HashMap<>();
        replacements.put("$restaurantName", restaurant.getName());
        replacements.put("$logoURL$", epicuriBaseURL + "/Restaurant/BillLogo/" + restaurant.getReceiptImageURL());
        replacements.put("$sessionNumber$", session.getReadableId());
        ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(session.getStartTime(), restaurant.getIANATimezone());
        String formattedTime = DATE_TIME_FORMATTER.format(restaurantTime);
        replacements.put("$sessionDateTime$", formattedTime);
        replacements.put("$addressLine1$", restaurant.getAddress().getStreet());
        replacements.put("$addressLine2$", restaurant.getAddress().getTown());
        replacements.put("$addressPostCode$", restaurant.getAddress().getPostcode());

        StringBuilder itemList = new StringBuilder();
        for(Order order : orders) {
            itemList.append("<tr class=\"item\"><td>");
            if(order.getMenuItem() != null) {
                itemList.append(order.getMenuItem().getName());
            } else {
                itemList.append("Unknown item");
            }
            itemList.append("</td><td>");
            itemList.append(formatter.format(MoneyService.toMoneyRoundNearest(SessionCalculationService.getOrderValue(order))));
            itemList.append("</td></tr>");
        }

        replacements.put("$itemList$", itemList.toString());
        replacements.put("$vatTotal$", formatter.format(MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.VAT_TOTAL).intValue())));
        replacements.put("$total$", formatter.format(MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TOTAL).intValue())));
        int paid = calculations.get(CalculationKey.TOTAL_PAYMENTS).intValue() - calculations.get(CalculationKey.CHANGE_DUE).intValue();
        replacements.put("$paid$", formatter.format(MoneyService.toMoneyRoundNearest(paid)));
        replacements.put("$tip$", formatter.format(MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.OVER_PAYMENTS_INCLUDING_TIP).intValue())));
        replacements.put("$discounts$", "-" + formatter.format(MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.DISCOUNT_TOTAL).intValue())));


        for(EmailRequest emailRequest : emailRequests) {
            if(StringUtils.isBlank(emailRequest.getEmail())) {
                continue;
            }

            replacements.put("$customerName$", Customer.determineName(emailRequest.getFirstName(), emailRequest.getLastName(), ""));
            replacements.put("$customerEmail$", emailRequest.getEmail());
            replacements.put("$customerPhoneNumber$", emailRequest.getPhoneNumber() == null ? "" : emailRequest.getPhoneNumber());

            emailService.createBuilder("Your eReceipt from " + restaurant.getName(),
                    emailRequest.getEmail(),
                    restaurant.getName(),
                    restaurant.getInternalEmailAddress() != null ? restaurant.getInternalEmailAddress() : restaurant.getPublicEmailAddress(),
                    "emails/email-receipt-to-diner-en.html",
                    "",
                    replacements).build();
        }

        return CompletableFuture.completedFuture(null);
    }

    private void sendRegistrationConfirmationToDiner(String email) {
        IMailBuilder builder = emailService.createBuilder("Woohoo! Welcome to the Epicuri club!",
                email,
                "Epicuri",
                "no-reply@epicuri.email",
                "emails/new-customer-registration-en.html",
                "Thanks for registering to Epicuri via the app or your recent restaurant booking. Please see epicuri.co.uk/guest.html for more information.",
                new HashMap<>());
        boolean sent = builder.build();
        if(!sent) {
            LOGGER.warn("Email for registration not sent to {}", email);
        }
    }

    private void sendEmailToRestaurant(Booking booking, Restaurant restaurant) {
        if(booking == null || restaurant == null) {
            return;
        }

        if(StringUtils.isEmpty(restaurant.getInternalEmailAddress()) && StringUtils.isEmpty(restaurant.getPublicEmailAddress())) {
            return;
        }

        Map<String,String> replacements = new HashMap<>();
        ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(booking.getTargetTime(), restaurant.getIANATimezone());
        String formattedTime = DATE_TIME_FORMATTER.format(restaurantTime);
        replacements.put("$bookingdt$", formattedTime);
        replacements.put("$restaurantName$", restaurant.getName());
        replacements.put("$numberOfPeople$", String.valueOf(booking.getNumberOfPeople()));
        replacements.put("$notes$", StringUtils.isBlank(booking.getNotes()) ? "None" : booking.getNotes());
        replacements.put("$dinerEmail$", booking.getEmail());
        replacements.put("$dinerNumber$", booking.getTelephone());
        replacements.put("$dinerName$", booking.getName());

        IMailBuilder builder = emailService.createBuilder("Your booking at " + restaurant.getName(),
                StringUtils.isEmpty(restaurant.getInternalEmailAddress()) ? restaurant.getPublicEmailAddress() : restaurant.getInternalEmailAddress(),
                "Epicuri Booking Manager",
                "no-reply@epicuri.email",
                "emails/restaurant-booking-confirm-en.html",
                createAlternateTextForRestaurant(booking, restaurant),
                replacements);
        boolean sent = builder.build();
        if(!sent) {
            LOGGER.warn("Email not sent to restaurant id {} for booking id {}", restaurant.getId(), booking.getId());
        }
    }

    private void sendEmailToDiner(Booking booking, Restaurant restaurant) {
        if(booking == null) {
            return;
        }

        Customer customer = null;
        if(booking.getCustomerId() != null) {
            customer = customerService.getCustomer(booking.getCustomerId());
        }

        String emailAddress;
        String name;
        if(customer == null || StringUtils.isBlank(customer.getEmail()) ) {
            if(StringUtils.isNotBlank(booking.getEmail())) {
                emailAddress = booking.getEmail();
                name = booking.getName();
            } else {
                LOGGER.warn("Could not send an email to customer for booking {} because there is no valid email address", booking.getId());
                return;
            }
        } else {
            emailAddress = customer.getEmail();
            name = customer.getFirstName();
        }

       if(StringUtils.isBlank(emailAddress)) {
           LOGGER.warn("Could not send an email to customer for booking {} because there is no valid email address", booking.getId());
           return;
       }

       sendEmailToDiner(name, emailAddress, booking, restaurant);
    }

    private void sendEmailToDiner(String firstName, String email, Booking booking, Restaurant restaurant) {
        Map<String,String> replacements = new HashMap<>();
        replacements.put("$dinerName$", StringUtils.isBlank(firstName) ? "!" : firstName);
        replacements.put("$numberOfPeople$", String.valueOf(booking.getNumberOfPeople()));
        replacements.put("$restaurantName$", restaurant.getName());
        ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(booking.getTargetTime(), restaurant.getIANATimezone());
        replacements.put("$bookingdt$", DATE_TIME_FORMATTER.format(restaurantTime));
        Address address = restaurant.getAddress();
        replacements.put("$addressLine1$", address.getStreet());
        replacements.put("$addressLine2$", address.getTown());
        replacements.put("$addressLine3$", address.getCity());
        replacements.put("$addressLine4$", address.getPostcode());
        replacements.put("$maplink$", "http://maps.google.com/maps?q=" + restaurant.getPosition().getLatitude() + "," + restaurant.getPosition().getLongitude());
        replacements.put("$restaurantNumber$", restaurant.getPhoneNumber1());
        replacements.put("$restaurantEmail$", restaurant.getPublicEmailAddress());

        String fromEmail = StringUtils.isBlank(restaurant.getPublicEmailAddress()) ? restaurant.getPublicEmailAddress() : "no-reply@epicuri.email";
        IMailBuilder builder = emailService.createBuilder("Your booking at " + restaurant.getName(),
                email,
                restaurant.getName(),
                fromEmail,
                "emails/diner-booking-confirm-en.html",
                createAlternateTextForDiner(booking, restaurant),
                replacements);
        boolean sent = builder.build();
        if(!sent) {
            LOGGER.warn("Email not sent to customer {} for booking id {}", email, booking.getId());
        }
    }

    private String createAlternateTextForDiner(Booking booking, Restaurant restaurant) {
        ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(booking.getTargetTime(), restaurant.getIANATimezone());
        return "Booking confirmed for " + DATE_TIME_FORMATTER.format(restaurantTime) + " under the name of \"" + booking.getName() + "\"";
    }

    private String createAlternateTextForRestaurant(Booking booking, Restaurant restaurant) {
        ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(booking.getTargetTime(), restaurant.getIANATimezone());
        return "Booking confirmed for " + DATE_TIME_FORMATTER.format(restaurantTime) + " under the name of \"" + booking.getName() + "\", " + booking.getNumberOfPeople() + " people.";
    }

    private void sendCashup(CashUp cashUp, Restaurant restaurant) {
        String emailAddress = restaurant.getInternalEmailAddress();
        if(emailAddress == null) {
            emailAddress = restaurant.getPublicEmailAddress();
        }

        String currency = "?";
        Currency currencyInstance = Currency.getInstance(restaurant.getISOCurrency());
        if(currencyInstance != null) {
            currency = currencyInstance.getSymbol();
        }

        ZonedDateTime startTime = TimeUtil.getRestaurantTime(cashUp.getStartTime(), restaurant.getIANATimezone());
        ZonedDateTime endTime = TimeUtil.getRestaurantTime(cashUp.getEndTime(), restaurant.getIANATimezone());
        Map<String,String> replacements = new HashMap<>();
        replacements.put("$cashupID$", cashUp.getId());
        String startDTString = DATE_TIME_FORMATTER.format(startTime);
        replacements.put("$startDateTime$", startDTString);
        String endDTString = DATE_TIME_FORMATTER.format(endTime);
        replacements.put("$endDateTime$", endDTString);
        replacements.put("$name$", restaurant.getName());
        replacements.put("$email$", emailAddress);
        replacements.put("$address$", restaurant.getAddress().prettyToString());
        replacements.put("$onPremiseCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.SEATED_SESSIONS_COUNT)));
        replacements.put("$onPremiseValue$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.SEATED_SESSIONS_VALUE)));
        replacements.put("$takeawaysCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT)));
        replacements.put("$takeawaysValue$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE)));
        replacements.put("$unpaidOnPremiseCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.VOID_SEATED_SESSION_COUNT)));
        replacements.put("$unpaidOnPremiseValue$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.VOID_SEATED_SESSION_VALUE)));
        replacements.put("$unpaidTakeawayCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT)));
        replacements.put("$unpaidTakeawayValue$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE)));
        replacements.put("$totalUnpaid$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.VOID_VALUE)));
        replacements.put("$foodCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.FOOD_COUNT)));
        replacements.put("$grossFoodAmount$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.FOOD_VALUE)));
        replacements.put("$drinkCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.DRINK_COUNT)));
        replacements.put("$grossDrinkAmount$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.DRINK_VALUE)));
        replacements.put("$otherCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.OTHER_COUNT)));
        replacements.put("$grossOtherAmount$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.OTHER_VALUE)));
        replacements.put("$deliveryCharges$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TOTAL_DELIVERY)));
        replacements.put("$totalSales$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TOTAL_SALES)));
        replacements.put("$totalSalesCount$", String.valueOf(cashUp.getReport().get(CashUpKeys.SEATED_SESSIONS_COUNT) + cashUp.getReport().get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT)));

        String itemFirst = "<tr class=\"item last\"><td>";
        String itemMiddle = "</td><td>";
        String itemLast = "</td></tr>";

        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String,Integer> entry : cashUp.getAdjustmentReport().entrySet()) {
            stringBuilder.append(itemFirst).append(entry.getKey()).append(itemMiddle).append(valueToDouble(currency, entry.getValue())).append(itemLast);
        }
        replacements.put("$adjustmentsList$", stringBuilder.toString());

        replacements.put("$totalAdjustments$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TOTAL_ADJUSTMENTS)));
        replacements.put("$totalSalesAfterAdjustments$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TOTAL_SALES) - cashUp.getReport().get(CashUpKeys.TOTAL_ADJUSTMENTS)));
        replacements.put("$totalVATCharged$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.VAT_VALUE)));
        replacements.put("$netSales$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.NET_VALUE)));

        stringBuilder = new StringBuilder();
        for(Map.Entry<String,Integer> entry : cashUp.getPaymentReport().entrySet()) {
            stringBuilder.append(itemFirst).append(entry.getKey()).append(itemMiddle).append(valueToDouble(currency, entry.getValue())).append(itemLast);
        }
        replacements.put("$paymentsList$", stringBuilder.toString());

        replacements.put("$tips$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.TOTAL_TIP)));
        replacements.put("$overpayments$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.OVER_PAYMENTS)));
        replacements.put("$totalPayments$", valueToDouble(currency, cashUp.getReport().get(CashUpKeys.PAYMENTS)));

        IMailBuilder builder = emailService.createBuilder("Cash-up: " + startDTString + " to " + endDTString,
                emailAddress,
                "Epicuri",
                "no-reply@epicuri.email",
                "emails/email-cashup-en.html",
                "Could not generate cashup",
                replacements);
        boolean sent = builder.build();
        if(!sent) {
            LOGGER.warn("Email not sent to restaurant id {} for cashup id {}", restaurant.getId(), cashUp.getId());
        }
    }

    private String valueToDouble(String currency, int pennies) {
        return currency + String.format("%.2f", MoneyService.toMoneyRoundNearest(pennies));
    }
}
