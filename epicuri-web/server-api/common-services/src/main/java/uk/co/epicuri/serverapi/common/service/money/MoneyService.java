package uk.co.epicuri.serverapi.common.service.money;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by manish
 *
 * Many of these are static because used in view pojos
 *
 * At some point we should probably use Joda Money but at this moment it's not really required
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

    public static int[] split(int amount, int diners) {
        if(amount == 0) return split0(diners);
        int value = BigDecimal.valueOf(amount).divideToIntegralValue(BigDecimal.valueOf(diners)).intValue();

        int[] splits = new int[diners];
        for(int i = 0; i < diners; i++) {
            splits[i] = value;
        }

        int leftOver = amount - (value * splits.length);
        splits[0] = splits[0] + leftOver;

        return splits;
    }

    public static double[] splitToMoney(int amount, int diners) {
        int[] splits = split(amount, diners);
        double[] moneySplits = new double[splits.length];

        for(int i = 0; i < splits.length; i++) {
            moneySplits[i] = toMoneyRoundNearest(splits[i]);
        }

        return moneySplits;
    }

    private static double[] splitToMoney0(int diners) {
        double[] zeros = new double[diners];
        Arrays.fill(zeros, 0D);
        return zeros;
    }
    private static int[] split0(int diners) {
        int[] zeros = new int[diners];
        Arrays.fill(zeros, 0);
        return zeros;
    }


    public static int percentageDiscountToInt(double percentage) {
        return (int)Math.round(percentage * 10);
    }

    public static double intToPercentageDiscount(int value) {
        return value / 10D;
    }
}
