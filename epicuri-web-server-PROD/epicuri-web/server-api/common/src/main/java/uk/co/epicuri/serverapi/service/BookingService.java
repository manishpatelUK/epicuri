package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.repository.BookingRepository;
import uk.co.epicuri.serverapi.repository.SessionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    @Lazy
    private AsyncCommunicationsService asyncCommunicationsService;

    @Autowired
    private SessionRepository sessionRepository;

    @Value("${epicuri.booking.limit}")
    private long bookingUpperLimit;

    @Autowired
    private Environment environment;

    private final long LIMIT = 1000 * 60 * 60 * 24; //24 hours

    private final FuseBox customerReservationCreationBlackoutsFuseBox;
    private final FuseBox customerReservationEditFuseBox;
    private final FuseBox customerReservationTimeCheckFuseBox;
    private final FuseBox customerReservationCreationFuseBox;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private List<String> ALL_RESERVATION_SLOTS = generateAllHalfHourSlots();
    private List<String> ALL_TAKEAWAY_SLOTS = generateAll15MinuteSlots();

    public BookingService() {
        this.customerReservationCreationFuseBox = new FuseBox();
        this.customerReservationEditFuseBox = new FuseBox();
        this.customerReservationTimeCheckFuseBox = new FuseBox();
        this.customerReservationCreationBlackoutsFuseBox = new FuseBox();

        //customer driven reservations fusebox
        customerReservationCreationFuseBox.add(FuseBox::checkTimeBeforeNow);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationMinTime);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationsBlackouts);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxCoversPerReservation);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxActiveReservations);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxActiveReservationsCovers);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationAlreadyExists);
        customerReservationCreationFuseBox.add(FuseBox::checkBlackMarksAnonymous);
        customerReservationCreationFuseBox.finalise();

        //customer driven reservations fusebox (editing)
        customerReservationEditFuseBox.add(FuseBox::checkTimeBeforeNow);
        customerReservationEditFuseBox.add(FuseBox::checkReservationMinTime);
        customerReservationEditFuseBox.add(FuseBox::checkReservationsBlackouts);
        customerReservationEditFuseBox.add(FuseBox::checkMaxCoversPerReservation);
        customerReservationEditFuseBox.add(FuseBox::checkMaxActiveReservations);
        customerReservationEditFuseBox.add(FuseBox::checkMaxActiveReservationsCovers);
        customerReservationEditFuseBox.add(FuseBox::checkBlackMarksAnonymous);
        customerReservationEditFuseBox.finalise();

        //booking widget time checks
        customerReservationTimeCheckFuseBox.add(FuseBox::checkTimeBeforeNow);
        customerReservationTimeCheckFuseBox.add(FuseBox::checkReservationMinTime);
        customerReservationTimeCheckFuseBox.add(FuseBox::checkReservationsBlackouts);
        customerReservationTimeCheckFuseBox.add(FuseBox::checkMaxCoversPerReservation);
        customerReservationTimeCheckFuseBox.add(FuseBox::checkMaxActiveReservations);
        customerReservationTimeCheckFuseBox.add(FuseBox::checkMaxActiveReservationsCovers);
        customerReservationTimeCheckFuseBox.finalise();

        //customer driven reservations fusebox for checking absolute rejections
        customerReservationCreationBlackoutsFuseBox.add(FuseBox::checkReservationsBlackouts);
        customerReservationCreationBlackoutsFuseBox.finalise();
    }

    private boolean isTestEnvironment() {
        for(String env : environment.getActiveProfiles()) {
            if (env.equals("test")) {
                return true;
            }
        }
        return false;
    }

    public Booking upsertReservation(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<Booking> getReservations(String restaurantId, long lower, long upper) {
        return bookingRepository.find(restaurantId, lower, upper, BookingType.RESERVATION, false);
    }

    public List<Booking> getTakeaways(String restaurantId, long lower, long upper) {
        return bookingRepository.find(restaurantId, lower, upper, BookingType.TAKEAWAY, false);
    }

    public List<Booking> getReservationsFrom(String restaurantId, long lower) {
        long upper = lower + bookingUpperLimit;
        return getReservations(restaurantId, lower, upper);
    }

    public List<Booking> getUpcomingReservations(String restaurantId) {
        long lower = System.currentTimeMillis() - LIMIT;
        long upper = lower + bookingUpperLimit;
        return getReservations(restaurantId, lower, upper);
    }

    public Booking getBooking(String id) {
        return bookingRepository.findOne(id,false,false);
    }

    public Booking getBookingIncludeCancelled(String id) {
        return bookingRepository.findOne(id,false,true);
    }

    public Booking upsert(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Booking insert(Booking booking) {
        return bookingRepository.insert(booking);
    }

    public List<Booking> getBookings(List<String> ids) {
        return Lists.newArrayList(bookingRepository.findAll(ids));
    }

    public void delete(String id) {
        bookingRepository.delete(id);
    }

    public void softDelete(String id) {
        bookingRepository.markDeleted(id, Booking.class);
    }

    public void acceptBooking(String id) {
        bookingRepository.pushAccept(id, true);
    }

    public void rejectBooking(String id, String reason) {
        bookingRepository.pushReject(id, true, reason);
    }

    public void cancelBooking(String id) {
        bookingRepository.pushCancel(id, true);
    }

    public List<Booking> getBookingsByCustomerId(String customerId, long lower, long upper, BookingType bookingType) {
        return bookingRepository.find(customerId, lower, upper, bookingType);
    }

    public Map<String,List<Booking>> getBookingsByCustomerId(String restaurantId, List<String> customerIds, long lower, long upper, boolean includeRejected) {
        List<Booking> bookings = bookingRepository.find(restaurantId, customerIds, lower, upper, includeRejected);
        //orders.stream().collect(Collectors.groupingBy(o -> printerIds.get(o.getMenuItemId())));
        if(bookings.size() == 0){
            return new HashMap<>();
        } else {
            return bookings.stream().collect(Collectors.groupingBy(Booking::getCustomerId));
        }
    }

    public boolean exists(String reservationId) {
        return bookingRepository.exists(reservationId);
    }

    public List<Booking> getBookings(String restaurantId, long start, long end, boolean includeRejected) {
        return bookingRepository.find(restaurantId, start, end, includeRejected);
    }

    public boolean checkTimesFuseBox(CustomerReservationView request) {
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, request);
        NoticeAggregator output = customerReservationTimeCheckFuseBox.check(false, fuseBoxAggregationProxy);
        return output.getIndividualNotices().size() > 0;
    }

    public boolean checkBlackoutsFuseBox(CustomerReservationView request) {
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, request);
        NoticeAggregator output = customerReservationCreationBlackoutsFuseBox.check(false, fuseBoxAggregationProxy);
        return output.getIndividualNotices().size() > 0;
    }

    public NoticeAggregator checkFuseBox(CustomerReservationView reservation, Customer customer) {
        return checkFuseBox(reservation, customer, customerReservationCreationFuseBox);
    }

    public NoticeAggregator checkFuseBoxForEdit(CustomerReservationView reservation, Customer customer) {
        return checkFuseBox(reservation, customer, customerReservationEditFuseBox);
    }

    private NoticeAggregator checkFuseBox(CustomerReservationView reservation, Customer customer, FuseBox fuseBox) {
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, reservation, customer);
        NoticeAggregator output = fuseBox.check(false, fuseBoxAggregationProxy);

        amendReservation(reservation, output);

        return output;
    }

    public NoticeAggregator checkFuseBox(CustomerReservationView reservation) {
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, reservation);
        NoticeAggregator output = customerReservationCreationFuseBox.check(false, fuseBoxAggregationProxy);

        amendReservation(reservation, output);

        return output;
    }

    public void amendReservation(CustomerReservationView reservation, NoticeAggregator output) {
        if(output.getIndividualNotices().size() > 0) {
            reservation.setAccepted(false);
        }
        else {
            reservation.setAccepted(true); //will be set back to false if reject notices are present
        }

        String rejectionNotice = output.getNotice();

        if(rejectionNotice != null) {
            reservation.setAccepted(false);
            reservation.setRejectionNotice(rejectionNotice);
        }
    }

    public void sendBookingConfirmations(Booking booking) {
        if(!isTestEnvironment()) {
            asyncCommunicationsService.sendBookingConfirmations(booking.getId(), booking.getRestaurantId());
        }
    }

    public void pushCustomerId(String bookingId, String customerId) {
        bookingRepository.pushCustomerId(bookingId, customerId);
    }

    public List<String> getAllReservationTimeSlots() {
        return ALL_RESERVATION_SLOTS;
    }

    public List<String> getAllTakeawayTimeSlots() {
        return ALL_TAKEAWAY_SLOTS;
    }

    public boolean isBookingTiedToSession(String bookingId) {
        return sessionRepository.findByOriginalBookingId(bookingId) != null;
    }

    private static List<String> generateAllHalfHourSlots() {
        List<String> allSlots = new ArrayList<>();

        allSlots.add("00:00");
        allSlots.add("01:00");
        allSlots.add("01:30");
        allSlots.add("02:00");
        allSlots.add("02:30");
        allSlots.add("03:00");
        allSlots.add("03:30");
        allSlots.add("04:00");
        allSlots.add("04:30");
        allSlots.add("05:00");
        allSlots.add("05:30");
        allSlots.add("06:00");
        allSlots.add("06:30");
        allSlots.add("07:00");
        allSlots.add("07:30");
        allSlots.add("08:00");
        allSlots.add("08:30");
        allSlots.add("09:00");
        allSlots.add("09:30");
        allSlots.add("10:00");
        allSlots.add("10:30");
        allSlots.add("11:00");
        allSlots.add("11:30");
        allSlots.add("12:00");
        allSlots.add("12:30");
        allSlots.add("13:00");
        allSlots.add("13:30");
        allSlots.add("14:00");
        allSlots.add("14:30");
        allSlots.add("15:00");
        allSlots.add("15:30");
        allSlots.add("16:00");
        allSlots.add("16:30");
        allSlots.add("17:00");
        allSlots.add("17:30");
        allSlots.add("18:00");
        allSlots.add("18:30");
        allSlots.add("19:30");
        allSlots.add("20:00");
        allSlots.add("20:30");
        allSlots.add("21:00");
        allSlots.add("21:30");
        allSlots.add("22:00");
        allSlots.add("22:30");
        allSlots.add("23:00");
        allSlots.add("23:30");

        return Collections.unmodifiableList(allSlots);
    }

    private static List<String> generateAll15MinuteSlots() {
        List<String> allSlots = new ArrayList<>();
        for(String hour : new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"}) {
            for(String minute : new String[]{"00","15","30","45"}) {
                allSlots.add(hour + ":" + minute);
            }
        }
        return Collections.unmodifiableList(allSlots);
    }
}
