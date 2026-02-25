package uk.co.epicuri.serverapi.common.pojo.host;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish.
 */
public class PrinterTicketsCourseView {
    private String course;
    private List<PrinterTicketView> items = new ArrayList<>();

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public List<PrinterTicketView> getItems() {
        return items;
    }

    public void setItems(List<PrinterTicketView> items) {
        this.items = items;
    }
}
