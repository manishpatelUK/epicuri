package uk.co.epicuri.serverapi.common.service.money;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class MoneyServiceTest {
    @Test
    public void testToPenniesRoundNearest() throws Exception {
        double[] vals = generateZeroToOne();
        for(int i = 0; i < vals.length; i++) {
            assertEquals(i, MoneyService.toPenniesRoundNearest(vals[i]));
        }

        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.999")), 100);
        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.998")), 100);
        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.995")), 100);
        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.994")), 99);
        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.991")), 99);
        assertEquals(MoneyService.toPenniesRoundNearest(Double.valueOf("0.99001")), 99);
    }

    @Ignore("Used by toPenniesRoundNearest")
    @Test
    public void testToPenniesRoundNearestBigDecimal() throws Exception {

    }

    @Ignore("Used by toPenniesRoundNearest")
    @Test
    public void testToPenniesRoundNearestBigDecimalPowerOfTen() throws Exception {

    }

    @Test
    public void testToMoneyRoundHalfDown() throws Exception {
        double[] vals = generateZeroToOne();
        for(int i = 0; i < vals.length; i++) {
            assertEquals(0, Double.compare(vals[i],MoneyService.toMoneyRoundHalfDown(i)));
        }
    }

    @Test
    public void testToMoneyRoundHalfUp() throws Exception {
        double[] vals = generateZeroToOne();
        for(int i = 0; i < vals.length; i++) {
            assertEquals(0, Double.compare(vals[i],MoneyService.toMoneyRoundHalfUp(i)));
        }
    }

    @Test
    public void testToMoneyRoundHalfEven() throws Exception {
        double[] vals = generateZeroToOne();
        for(int i = 0; i < vals.length; i++) {
            assertEquals(0, Double.compare(vals[i],MoneyService.toMoneyRoundHalfEven(i)));
        }
    }

    @Test
    public void testToMoneyRoundNearest() throws Exception {
        double[] vals = generateZeroToOne();
        for(int i = 0; i < vals.length; i++) {
            assertEquals(0, Double.compare(vals[i],MoneyService.toMoneyRoundNearest(i)));
        }
    }

    @Test
    public void testSplit() throws Exception {
        double[] split1 = MoneyService.splitToMoney(9,3);
        assertEquals(3, split1.length);
        for(double d : split1) {
            assertEquals(0, Double.compare(Double.valueOf("0.03"),d));
        }

        double[] split2 = MoneyService.splitToMoney(100,3);
        assertEquals(3, split2.length);
        assertEquals(0, Double.compare(Double.valueOf("0.34"),split2[0]));
        assertEquals(0, Double.compare(Double.valueOf("0.33"),split2[1]));
        assertEquals(0, Double.compare(Double.valueOf("0.33"),split2[2]));

        double[] split3 = MoneyService.splitToMoney(50,2);
        assertEquals(2, split3.length);
        assertEquals(0, Double.compare(Double.valueOf("0.25"),split3[0]));
        assertEquals(0, Double.compare(Double.valueOf("0.25"),split3[1]));

        double[] split4 = MoneyService.splitToMoney(8,3);
        assertEquals(3, split4.length);
        assertEquals(0, Double.compare(Double.valueOf("0.04"),split4[0]));
        assertEquals(0, Double.compare(Double.valueOf("0.02"),split4[1]));
        assertEquals(0, Double.compare(Double.valueOf("0.02"),split4[2]));

        double[] split5 = MoneyService.splitToMoney(113,7);
        assertEquals(7, split5.length);
        for(int i = 0; i < split5.length; i++) {
            if(i == 0) {
                assertEquals(0, Double.compare(Double.valueOf("0.17"),split5[i]));
            } else {
                assertEquals(0, Double.compare(Double.valueOf("0.16"), split5[i]));
            }
        }
    }

    private double[] generateZeroToOne() {
        double[] vals = new double[100];
        for(int i = 0; i < vals.length; i++) {
            String n = "0.";
            if(i < 10) {
                n += "0";
            }
            n += i;
            vals[i] = Double.valueOf(n);
        }
        return vals;
    }

}