package uk.co.epicuri.serverapi.common.pojo.model.session;

/**
 * Created by manish
 */
public enum  CalculationKey {
    TOTAL, //used in waiter app
    TOTAL_BEFORE_TIP,
    TOTAL_BEFORE_ADJUSTMENTS,SUB_TOTAL, // same in cpe  //used in waiter app
    VAT_TOTAL_BEFORE_ADJUSTMENTS,VAT_TOTAL, //same in cpe //used in waiter app
    VAT_DELIVERY,
    TIP_TOTAL,//used in waiter app
    TIP_PERCENTAGE,//used in waiter app
    TIP_BEFORE_DISCOUNT,
    DELIVERY_TOTAL,//used in waiter app
    SESSION_TOTAL,
    DISCOUNT_TOTAL,//used in waiter app
    NON_DEFERRED_DISCOUNTS_VALUE,
    REMAINING_TOTAL,//used in waiter app
    OVER_PAYMENTS,//used in waiter app
    OVER_PAYMENTS_INCLUDING_TIP,
    CHANGE_DUE,//used in waiter app
    TOTAL_PAYMENTS,
    TOTAL_PAYMENTS_CHANGEABLE,
    TOTAL_PAYMENTS_NOT_CHANGEABLE,
    GRATUITIES,
    NUMBER_OF_GUESTS,
    TOTAL_PAYMENTS_VOIDED
}
