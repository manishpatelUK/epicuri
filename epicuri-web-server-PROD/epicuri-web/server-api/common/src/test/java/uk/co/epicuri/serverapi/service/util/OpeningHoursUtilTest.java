package uk.co.epicuri.serverapi.service.util;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;

import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class OpeningHoursUtilTest {

    @Test
    public void isOverlapping() throws Exception {
        //head and tail overlaps
        HourSpan h1 = new HourSpan(9,0,12,0);
        HourSpan h2 = new HourSpan(11,0,13,0);
        assertTrue(OpeningHoursUtil.isOverlapping(h1,h2));
        assertTrue(OpeningHoursUtil.isOverlapping(h2,h1));

        h1 = new HourSpan(9,0,12,0);
        h2 = new HourSpan(6,0,10,0);
        assertTrue(OpeningHoursUtil.isOverlapping(h1,h2));
        assertTrue(OpeningHoursUtil.isOverlapping(h2,h1));

        //"inside" overlaps
        h1 = new HourSpan(9,0,12,0);
        h2 = new HourSpan(10,0,11,0);
        assertTrue(OpeningHoursUtil.isOverlapping(h1,h2));
        assertTrue(OpeningHoursUtil.isOverlapping(h2,h1));

        // non overlapping
        h1 = new HourSpan(9,0,12,0);
        h2 = new HourSpan(6,0,9,0);
        assertFalse(OpeningHoursUtil.isOverlapping(h1,h2));
        assertFalse(OpeningHoursUtil.isOverlapping(h2,h1));
    }

    @Test
    public void calculateMinutes() throws Exception {
        HourSpan h1 = new HourSpan(9,0,12,0);
        HourSpan h2 = new HourSpan(11,0,13,0);
        int[] calculated = OpeningHoursUtil.calculateMinutes(h1,h2);
        assertEquals(9*60, calculated[0]);
        assertEquals(12*60, calculated[1]);
        assertEquals(11*60, calculated[2]);
        assertEquals(13*60, calculated[3]);
    }

    @Test
    public void isTailOverlapping() throws Exception {
        HourSpan h1 = new HourSpan(9,0,12,0);
        HourSpan h2 = new HourSpan(11,0,13,0);
        assertTrue(OpeningHoursUtil.isTailOverlapping(h1,h2));
        assertTrue(OpeningHoursUtil.isTailOverlapping(h2,h1));
    }

    @Test
    public void isInside() throws Exception {
        HourSpan h1 = new HourSpan(9,0,12,0);
        HourSpan h2 = new HourSpan(10,0,11,0);
        assertTrue(OpeningHoursUtil.isInside(h1,h2));
        assertTrue(OpeningHoursUtil.isInside(h2,h1));
    }

    @Test
    public void joinAdjacent() throws Exception {
        //head and tail overlaps
        HourSpan h1 = new HourSpan(9,0,12,30);
        HourSpan h2 = new HourSpan(11,0,13,0);
        OpeningHoursUtil.joinAdjacent(h1,h2);
        assertAdjacentSplitFromOverlap(h1, h2);

        h1 = new HourSpan(9,0,12,30);
        h2 = new HourSpan(11,0,13,0);
        OpeningHoursUtil.joinAdjacent(h2,h1);
        assertAdjacentSplitFromOverlap(h1, h2);

        //"inside" overlaps
        h1 = new HourSpan(9,0,12,30);
        h2 = new HourSpan(10,0,11,0);
        OpeningHoursUtil.joinAdjacent(h2,h1);
        assertAdjacentSplitFromInside(h1, h2);

        h1 = new HourSpan(9,0,12,30);
        h2 = new HourSpan(10,0,11,0);
        OpeningHoursUtil.joinAdjacent(h2,h1);
        assertAdjacentSplitFromInside(h1, h2);
    }

    private void assertAdjacentSplitFromInside(HourSpan h1, HourSpan h2) {
        assertEquals(11, h1.getHourOpen());
        assertEquals(0, h1.getMinuteOpen());
        assertEquals(12, h1.getHourClose());
        assertEquals(30, h1.getMinuteClose());
        assertEquals(9, h2.getHourOpen());
        assertEquals(0, h2.getMinuteOpen());
        assertEquals(11, h2.getHourClose());
        assertEquals(0, h2.getMinuteClose());
    }

    private void assertAdjacentSplitFromOverlap(HourSpan h1, HourSpan h2) {
        assertEquals(9, h1.getHourOpen());
        assertEquals(0, h1.getMinuteOpen());
        assertEquals(11, h1.getHourClose());
        assertEquals(0, h1.getMinuteClose());
        assertEquals(11, h2.getHourOpen());
        assertEquals(0, h2.getMinuteOpen());
        assertEquals(13, h2.getHourClose());
        assertEquals(0, h2.getMinuteClose());
    }

}