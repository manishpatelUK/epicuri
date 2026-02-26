package com.thinktouchsee.bookingwidget.ui;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Reservation;
import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.*;
import java.util.Calendar;

/**
 * 26/08/2014
 */
public class BookingWidgetLayout extends VerticalLayout {

    private final List<Restaurant> restaurants;
    private Restaurant selectedRestaurant;
    private ComboBox hoursBox;
    private ComboBox minutesBox;
    private final DateField calendar;
    private final Button submit;
    private final TextField customerName, notesField, numberOfPeople, phone, email;
    private final DateTimeFormatter dtf1 = DateTimeFormat.forPattern("dd MMM yyyy");
    private final DateTimeFormatter dtf2 = DateTimeFormat.forPattern("HH:mm");

    private final EpicuriAPI api;
    private final String timezone;
    private final Collection<String> sendEmails;

    private final String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    public BookingWidgetLayout(final List<Restaurant> restaurants, final EpicuriAPI api, final String timezone, final Collection<String> sendEmails) {
        this.restaurants = restaurants;
        this.api = api;
        this.timezone = timezone;
        this.sendEmails = sendEmails;

        setImmediate(true);
        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(true);

        addComponent(formLayout);

        customerName = new TextField("Your Name");
        formLayout.addComponent(customerName);

        if(restaurants.size() > 1) {
            ComboBox pickRestaurantBox = getRestaurantsComboBox(restaurants);
            formLayout.addComponent(pickRestaurantBox);
        }
        else {
            selectedRestaurant = restaurants.get(0);
        }

        calendar = new DateField("Booking Date");
        calendar.setValue(new Date());

        formLayout.addComponent(calendar);

        HorizontalLayout times = new HorizontalLayout();
        hoursBox = getHoursBox();
        minutesBox = getMinutesBox();
        times.addComponent(hoursBox);
        times.addComponent(minutesBox);
        times.setCaption("Time");
        formLayout.addComponent(times);

        numberOfPeople = new TextField("Number of Guests");
        numberOfPeople.setImmediate(true);
        numberOfPeople.setValue("1");
        formLayout.addComponent(numberOfPeople);

        phone = new TextField("Phone");
        phone.addValidator(new RegexpValidator("[+][\\d]+", true, ""));
        formLayout.addComponent(phone);

        email = new TextField("Email");
        formLayout.addComponent(email);

        notesField = new TextField("Notes");
        formLayout.addComponent(notesField);

        submit = new Button("Book!");

        submit.addStyleName("style-primary");
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");
        submit.setWidth("179px");
        buttonLayout.addComponent(submit);
        buttonLayout.setComponentAlignment(submit,Alignment.MIDDLE_CENTER);
        addComponent(buttonLayout);
        setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

        submit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (selectedRestaurant == null) {
                    return;
                }

                if (StringUtils.isBlank(customerName.getValue())) {
                    Notification.show(selectedRestaurant.getName(), "Name required!", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (calendar.getValue() == null) {
                    Notification.show(selectedRestaurant.getName(), "Select a date!", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (StringUtils.isBlank(numberOfPeople.getValue()) || !numberOfPeople.getValue().matches("[\\d]+")) {
                    Notification.show(selectedRestaurant.getName(), "Number of guests required!", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (StringUtils.isBlank(phone.getValue())) {
                    String phoneValue = phone.getValue();
                    try {
                        phoneValue = phoneValue.replace("(", "");
                        phoneValue = phoneValue.replace(")", "");
                        phoneValue = phoneValue.replace("+", "00");
                    }catch(Exception ex){}
                    if(!phoneValue.matches("[\\d]+")) {
                        Notification.show(selectedRestaurant.getName(), "Valid phone number required!", Notification.Type.ERROR_MESSAGE);
                        return;
                    }
                }

                // check if there is availability
                int hours = Integer.parseInt((String) hoursBox.getValue());
                int minutes = Integer.parseInt((String) minutesBox.getValue());

                //check if restaurant object has a timezone - if so, that's the one to use
                DateTimeZone tz = selectedRestaurant.getTimezone() == null ? DateTimeZone.forID(timezone) : DateTimeZone.forID(selectedRestaurant.getTimezone());
                java.util.Calendar cal = Calendar.getInstance();
                cal.setTime(calendar.getValue());

                DateTime requestedLocalTime = new DateTime(tz).withDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH)).withTime(hours, minutes,0,0);
                DateTime requestedTimeUTC = requestedLocalTime.toDateTime(DateTimeZone.UTC);
                DateTime nowLocal = new DateTime(tz);

                if (requestedLocalTime.isBefore(nowLocal)) {
                    Notification.show(selectedRestaurant.getName(), "Cannot create a booking in the past.", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (requestedLocalTime.isBefore(nowLocal.plusHours(2))) {
                    Notification.show(selectedRestaurant.getName(), "Sorry, that's too soon!", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                Authentication authentication = api.login(String.valueOf(selectedRestaurant.getId()), "epicuriadmin", "keshavroshan");
                if (authentication == null) {
                    Notification.show(selectedRestaurant.getName(), "Not available for bookings, please call: "
                            + selectedRestaurant.getPhoneNumber()
                            + "; or choose another date and time", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                ReservationRequest reservationRequest = new ReservationRequest();
                reservationRequest.setName(customerName.getValue());
                reservationRequest.setNotes(notesField.getValue());
                reservationRequest.setNumberOfPeople(Integer.parseInt(numberOfPeople.getValue()));
                reservationRequest.setTelephone(phone.getValue());
                reservationRequest.setReservationTime(requestedTimeUTC.getMillis() / 1000);

                Reservation try1 = api.createReservation(reservationRequest, authentication.getAuthKey(), String.valueOf(selectedRestaurant.getId()), false);
                if(try1.isRejected() || !try1.isAccepted() || StringUtils.isNotBlank(try1.getRejectionNotice())) {
                    Notification.show(selectedRestaurant.getName(), "Sorry! We have no availability at this time. Please call: " + selectedRestaurant.getPhoneNumber(), Notification.Type.ERROR_MESSAGE);
                }
                else {
                    try {
                        Reservation reservation = api.createReservation(reservationRequest, authentication.getAuthKey(), String.valueOf(selectedRestaurant.getId()), true);
                        if (reservation.isAccepted()) {
                            Thread thread = new Thread(new SendEmailThread(requestedLocalTime, reservationRequest, selectedRestaurant));
                            thread.start();
                            finishUp();
                        } else {
                            Notification.show(selectedRestaurant.getName(), "Sorry! We have no availability at this time. Please call: " + selectedRestaurant.getPhoneNumber(), Notification.Type.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        Notification.show(selectedRestaurant.getName(), "Sorry! We have no availability at this time. Please call: " + selectedRestaurant.getPhoneNumber(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            }
        });

        /*try {
            HorizontalLayout downloads = getDownloadsLayout();
            addComponent(downloads);
            setComponentAlignment(downloads,Alignment.MIDDLE_CENTER);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    private HorizontalLayout getDownloadsLayout() {
        FileResource android = new FileResource(
                new File(basePath + File.separator + "VAADIN"
                                 + File.separator + "images"
                                 + File.separator + "android.png"));

        Link linkToPlay = new Link(null, new ExternalResource("https://play.google.com/store/apps/details?id=uk.co.epicuri.android&hl=en_GB"));
        linkToPlay.setIcon(android);
        linkToPlay.setTargetName("_blank");

        FileResource apple = new FileResource(
                new File(basePath + File.separator + "VAADIN"
                        + File.separator + "images"
                        + File.separator + "apple.png"));
        Link linkToApple = new Link(null, new ExternalResource("https://itunes.apple.com/gb/app/epicuri/id849250056"));
        linkToApple.setIcon(apple);
        linkToApple.setTargetName("_blank");

        FileResource epicuri = new FileResource(
                new File(basePath + File.separator + "VAADIN"
                        + File.separator + "images"
                        + File.separator + "logo_small_jpg-BW.jpg"));
        Link linkToEpicuri = new Link(null, new ExternalResource("http://guest.epicuri.co.uk"));
        linkToEpicuri.setIcon(epicuri);
        linkToEpicuri.setTargetName("_blank");

        HorizontalLayout downloads = new HorizontalLayout();
        downloads.addComponent(linkToPlay);
        downloads.addComponent(linkToApple);
        downloads.addComponent(linkToEpicuri);
        return downloads;
    }



    private void finishUp() {
        removeAllComponents();
        Label thanks = new Label("<h3><i>Thanks! See you soon!</i></h3>", ContentMode.HTML);
        thanks.setWidth("100%");
        addComponent(thanks);
        setComponentAlignment(thanks,Alignment.MIDDLE_CENTER);

        /*HorizontalLayout downloads = getDownloadsLayout();
        addComponent(downloads);
        setComponentAlignment(downloads,Alignment.MIDDLE_CENTER);*/
    }

    private ComboBox getRestaurantsComboBox(List<Restaurant> restaurants) {
        final BeanItemContainer<Restaurant> beanItemContainer = new BeanItemContainer<Restaurant>(Restaurant.class);
        beanItemContainer.addAll(restaurants);
        ComboBox box = new ComboBox("Restaurant");
        box.setImmediate(true);
        box.setContainerDataSource(beanItemContainer);
        box.setNullSelectionAllowed(false);
        box.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        box.setItemCaptionPropertyId("Name");

        box.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                selectedRestaurant = (Restaurant) valueChangeEvent.getProperty().getValue();
            }
        });


        box.select(beanItemContainer.getItemIds().get(0));

        return box;
    }

    private ComboBox getHoursBox() {
        ComboBox box = new ComboBox();
        box.setNullSelectionAllowed(false);
        box.setWidth("60px");
        box.addItem("10");
        box.addItem("11");
        box.addItem("12");
        box.addItem("13");
        box.addItem("14");
        box.addItem("15");
        box.addItem("16");
        box.addItem("17");
        box.addItem("18");
        box.addItem("19");
        box.addItem("20");
        box.addItem("21");
        box.addItem("22");

        box.setValue("12");

        box.setPageLength(4);

        return box;
    }

    private ComboBox getMinutesBox() {
        ComboBox box = new ComboBox();
        box.setNullSelectionAllowed(false);
        box.setWidth("60px");
        box.addItem("00");
        box.addItem("30");

        box.setValue("00");

        box.setPageLength(4);

        return box;
    }

    private class SendEmailThread implements Runnable {
        private final DateTime requestedTime;
        private final ReservationRequest reservationRequest;
        private final Restaurant restaurant;

        public SendEmailThread(DateTime requestedTime, ReservationRequest reservationRequest, Restaurant restaurant) {

            this.requestedTime = requestedTime;
            this.reservationRequest = reservationRequest;
            this.restaurant = restaurant;
        }

        public void run() {
            try {
                sendEmailToDiner(restaurant, email.getValue(), requestedTime, reservationRequest);

                if(sendEmails.size() > 0) {
                    for (String e : sendEmails) {
                        sendEmailToRestaurant(restaurant, e, requestedTime, reservationRequest);
                    }
                }
                else if(StringUtils.isNotBlank(restaurant.getEmail())) {
                    sendEmailToRestaurant(restaurant, restaurant.getEmail(), requestedTime, reservationRequest);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        private void sendEmailToDiner(Restaurant restaurant, String email, DateTime requestTime, ReservationRequest request) {

            if(StringUtils.isBlank(email)) {
                return;
            }

            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter("api","key-40c450e79f813169a600b6e155e94b5c"));
            WebResource webResource = client.resource("https://api.mailgun.net/v2/epicuri.email/messages");
            FormDataMultiPart formData = new FormDataMultiPart();
            String fromEmail = StringUtils.isBlank(restaurant.getEmail()) ? restaurant.getEmail() : "no-reply@epicuri.email";
            formData.field("from", restaurant.getName() + " <" + fromEmail + ">");
            formData.field("to", request.getName() + " <" + email + ">");
            formData.field("subject", "You're all set for " + restaurant.getName() + "!");

            formData.field("html", "Dear " + request.getName());
            formData.field("html", "</p>");
            formData.field("html", "Your table for " + request.getNumberOfPeople()
                    + " at " + restaurant.getName()
                    + " will be ready on "
                    + dtf1.print(requestTime) + " at "
                    + dtf2.print(requestTime) + ". "
                    + "The reservation is held under: " + request.getName());
            formData.field("html", "</p>");
            formData.field("html", "To get there:");
            formData.field("html", "</p>");

            if(StringUtils.isNotBlank(restaurant.getAddress().getStreet())) {
                formData.field("html", restaurant.getAddress().getStreet());
            }
            if(StringUtils.isNotBlank(restaurant.getAddress().getTown())) {
                formData.field("html", restaurant.getAddress().getTown());
            }
            if(StringUtils.isNotBlank(restaurant.getAddress().getCity())) {
                formData.field("html", restaurant.getAddress().getCity());
            }
            if(StringUtils.isNotBlank(restaurant.getAddress().getPostCode())) {
                formData.field("html", restaurant.getAddress().getPostCode());
            }

            formData.field("html", "</p>");

            if(StringUtils.isNotBlank(restaurant.getPhoneNumber())) {
                formData.field("html", restaurant.getPhoneNumber());
            }
            if(StringUtils.isNotBlank(restaurant.getEmail())) {
                formData.field("html","<a href='"+restaurant.getEmail()+"'>"+restaurant.getEmail()+"</a>");
            }

            formData.field("html", "</p>");
            formData.field("html", "Should your plans change, please let us know.");
            formData.field("html", "</p>");
            formData.field("html", "<html><h3>Using the web is so 2005!</h3></p>Why not make and manage your bookings using our <strong>free</strong> Epicuri guest app? Don't forget to check into the " + restaurant.getName() + " when you arrive, <strong>you'll get superpowers!</strong></html>");
            formData.field("html", "</p>");
            formData.field("html", "<html>Download for iOS <a href='https://itunes.apple.com/gb/app/epicuri/id849250056'>here</a></html>");
            formData.field("html", "<html>Download for Android <a href='https://play.google.com/store/apps/details?id=uk.co.epicuri.android&hl=en_GB'>here</a></html>");

            ClientResponse response = null;

            try {
                response = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formData);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                if(response != null) {
                    try {
                        response.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        private void sendEmailToRestaurant(Restaurant restaurant, String restaurantEmailAddress, DateTime requestTime, ReservationRequest request) {
            if(StringUtils.isBlank(restaurantEmailAddress)) {
                return;
            }

            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter("api","key-40c450e79f813169a600b6e155e94b5c"));
            WebResource webResource = client.resource("https://api.mailgun.net/v2/epicuri.email/messages");
            FormDataMultiPart formData = new FormDataMultiPart();
            String fromEmail = "no-reply@epicuri.email";
            formData.field("from", "Epicuri Booking Manager <" + fromEmail + ">");
            formData.field("to", request.getName() + " <" + restaurantEmailAddress + ">");
            formData.field("subject", "New reservation: " + dtf1.print(requestTime));

            formData.field("htm;", "Hi " + restaurant.getName());
            formData.field("html", "</p>");
            formData.field("html", "You have a new table reservation!");
            formData.field("html", "</p>");
            formData.field("html", "Reservation Name: " + reservationRequest.getName());
            formData.field("html", dtf1.print(requestTime) + " at " + dtf2.print(requestTime) + ". Party of " + request.getNumberOfPeople());
            if(StringUtils.isNotBlank(reservationRequest.getNotes())) {
                formData.field("html", "<i>Notes: " + reservationRequest.getNotes() + "</i>");
            }
            if(StringUtils.isNotBlank(email.getValue())) {
                formData.field("html", "Email: " + email.getValue());
            }
            if(StringUtils.isNotBlank(phone.getValue())) {
                formData.field("html", "Phone: " + phone.getValue());
            }
            formData.field("html", "</p>");
            formData.field("html", "The reservation is <strong>confirmed</strong> and in your Epicuri calendar.");
            formData.field("html", "</p>");
            formData.field("html", "Use your Epicuri app to manage bookings or to get a full up-to-the-second view of the day's reservations");
            formData.field("html", "</p>");
            formData.field("html", "Best Regards");
            formData.field("html", "</p>");
            formData.field("html", "Epicuri Booking Manager");

            ClientResponse response = null;
            try {
                response = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formData);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                if(response != null) {
                    try {
                        response.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
