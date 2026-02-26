package uk.co.epicuri.waiter.service;

import java.math.BigDecimal;

/**
 * Created by manish on 19/12/2017.
 */

public class MoneyService {
    public static int toPenniesRoundNearest(double money) {
        return toPenniesRoundNearest(BigDecimal.valueOf(money).scaleByPowerOfTen(2));
    }

    public static int toPenniesRoundNearest(BigDecimal number) {
        return number.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static int toPenniesRoundNearest(BigDecimal number, int powerOfTen) {
        return number.scaleByPowerOfTen(powerOfTen).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static double toMoneyRoundHalfDown(int pennies) {
        return BigDecimal.valueOf(pennies).scaleByPowerOfTen(-2).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    public static double toMoneyRoundHalfUp(int pennies) {
        return BigDecimal.valueOf(pennies).scaleByPowerOfTen(-2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double toMoneyRoundHalfEven(int pennies) {
        return BigDecimal.valueOf(pennies).scaleByPowerOfTen(-2).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    public static double toMoneyRoundNearest(int pennies) {
        return toMoneyRoundHalfEven(pennies);
    }

    public static int discountValue(double adjustmentValue, int operatingValue) {
        adjustmentValue = Math.min(100D,Math.abs(adjustmentValue));
        BigDecimal value = BigDecimal.valueOf(adjustmentValue/100D).multiply(BigDecimal.valueOf(operatingValue));
        return toPenniesRoundNearest(value);
    }

}
