package uk.co.epicuri.serverapi.common.pojo.common;

import java.util.HashMap;
import java.util.Map;

public class BillSplit {
    private Map<String,Integer> itemSplits = new HashMap<>(); //items for each diner only
    private Map<String,Integer> tableItemSplits = new HashMap<>(); //items for each diner from table (not including default diner)
    private Map<String,Integer> tipSplits = new HashMap<>(); //amount of tip for each diner
    private Map<String,Integer> discountSplits = new HashMap<>(); //amount of discount for each diner
    private Map<String,Integer> equalSplits = new HashMap<>(); //amount each diner pays if it was an equal split
    private Map<String,Integer> vatSplits = new HashMap<>(); //amount each diner pays in vat

    public Map<String, Integer> getItemSplits() {
        return itemSplits;
    }

    public void setItemSplits(Map<String, Integer> itemSplits) {
        this.itemSplits = itemSplits;
    }

    public Map<String, Integer> getTableItemSplits() {
        return tableItemSplits;
    }

    public void setTableItemSplits(Map<String, Integer> tableItemSplits) {
        this.tableItemSplits = tableItemSplits;
    }

    public Map<String, Integer> getTipSplits() {
        return tipSplits;
    }

    public void setTipSplits(Map<String, Integer> tipSplits) {
        this.tipSplits = tipSplits;
    }

    public Map<String, Integer> getDiscountSplits() {
        return discountSplits;
    }

    public void setDiscountSplits(Map<String, Integer> discountSplits) {
        this.discountSplits = discountSplits;
    }

    public Map<String, Integer> getEqualSplits() {
        return equalSplits;
    }

    public void setEqualSplits(Map<String, Integer> equalSplits) {
        this.equalSplits = equalSplits;
    }

    public Map<String, Integer> getVatSplits() {
        return vatSplits;
    }

    public void setVatSplits(Map<String, Integer> vatSplits) {
        this.vatSplits = vatSplits;
    }
}
