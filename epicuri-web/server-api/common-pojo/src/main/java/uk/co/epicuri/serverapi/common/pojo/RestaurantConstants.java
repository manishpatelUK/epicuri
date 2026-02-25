package uk.co.epicuri.serverapi.common.pojo;

import uk.co.epicuri.serverapi.common.pojo.model.Course;

public class RestaurantConstants {
    public static final String IMMEDIATE_COURSE_NAME = "ASAP";
    public static final String FALLBACK_COURSE_NAME = "ASAP";
    public static final Course FALLBACK_COURSE = createFallbackCourse();

    public static final String REFUND_ADJUSTMENT = "REFUND";

    public static final String STAFF_PRINT_LABEL = "STAFF";
    public static final String CUSTOMER_PRINT_LABEL = "CUSTOMER";

    public static final String DEFAULT_DINER_NAME = "Table";
    public static final String ACTUAL_DINER_PREPEND = "Guest";

    public static final String DEFER_ADJUSTMENT = "CUSTOMER ACCOUNT";
    public static final String ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE = "DEFERMENT_NOTE_CUSTOMER_INFO";

    private static Course createFallbackCourse() {
        Course course = new Course();
        course.setId("-1");
        course.setName(FALLBACK_COURSE_NAME);
        course.setOrdering((short)0);
        return course;
    }
}
