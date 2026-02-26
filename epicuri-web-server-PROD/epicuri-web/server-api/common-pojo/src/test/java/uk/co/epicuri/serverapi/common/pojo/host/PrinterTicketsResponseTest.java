package uk.co.epicuri.serverapi.common.pojo.host;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class PrinterTicketsResponseTest {
    @Test
    public void compareTo1() throws Exception {
        PrinterTicketsResponse p1 = createResponse(false);
        PrinterTicketsResponse p2 = createResponse(true);

        List<PrinterTicketsResponse> list = Lists.newArrayList(p1, p2);
        Collections.sort(list);

        assertEquals(p1, list.get(0));
        assertEquals(p2, list.get(1));

        list = Lists.newArrayList(p2, p1);
        Collections.sort(list);

        assertEquals(p1, list.get(0));
        assertEquals(p2, list.get(1));
    }

    @Test
    public void compareTo2() throws Exception {
        PrinterTicketsResponse p1 = createResponse(false, 10, 5, 30);
        PrinterTicketsResponse p2 = createResponse(false, 50, 80, 1);
        PrinterTicketsResponse p3 = createResponse(false, 4, 5, 6);

        List<PrinterTicketsResponse> list = Lists.newArrayList(p1, p2,p3);
        Collections.sort(list);

        assertEquals(p2, list.get(0));
        assertEquals(p1, list.get(1));
        assertEquals(p3, list.get(2));
    }

    @Test
    public void compareTo3() throws Exception {
        PrinterTicketsResponse p1 = createResponse(true, 10, 5, 30);
        PrinterTicketsResponse p2 = createResponse(false, 50, 80, 1);
        PrinterTicketsResponse p3 = createResponse(false, 4, 5, 6);

        List<PrinterTicketsResponse> list = Lists.newArrayList(p1, p2,p3);
        Collections.sort(list);

        assertEquals(p2, list.get(0));
        assertEquals(p3, list.get(1));
        assertEquals(p1, list.get(2));
    }

    private PrinterTicketsResponse createResponse(boolean isDone, long... items) {
        PrinterTicketsResponse p1 = new PrinterTicketsResponse();
        p1.setDone(isDone);

        PrinterTicketsCourseView printerTicketsCourseView = new PrinterTicketsCourseView();
        p1.getCourses().add(printerTicketsCourseView);
        for(long item : items) {
            PrinterTicketView printerTicketView = new PrinterTicketView();
            printerTicketView.setCreationTime(item);
            printerTicketsCourseView.getItems().add(printerTicketView);
        }

        return p1;
    }

}