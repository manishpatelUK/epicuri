package uk.co.epicuri.serverapi.host.util;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by manish on 15/07/2017.
 */
public class PrinterUtilTest {
    @Test
    public void isCyclic() throws Exception {
        Printer printer1 = new Printer();
        printer1.setId("1");
        Printer printer2 = new Printer();
        printer2.setId("2");
        Printer printer3 = new Printer();
        printer3.setId("3");

        Map<String,Printer> map = new HashMap<>();
        map.put(printer1.getId(), printer1);
        map.put(printer2.getId(), printer2);
        map.put(printer3.getId(), printer3);

        assertFalse(PrinterUtil.isCyclic(printer1, printer2, map));

        printer1.setRedirect(PrinterUtil.redirectId(printer1.getId(), printer2.getId()));

        assertFalse(PrinterUtil.isCyclic(printer1, printer2, map));
        assertTrue(PrinterUtil.isCyclic(printer2, printer1, map));
    }

}