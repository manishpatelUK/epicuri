package uk.co.epicuri.serverapi.common.pojo.external.xero;

import uk.co.epicuri.serverapi.common.pojo.host.HostAdjustmentTypeView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XeroMappingsResponse {
    private List<XeroAccountView> accounts = new ArrayList<>();
    private List<HostAdjustmentTypeView> allAdjustmentTypes = new ArrayList<>();
    private List<HostTaxView> allTaxes = new ArrayList<>();
    private List<XeroMappingRule> rules = new ArrayList<>();
    private List<String> itemTypes = createItemTypes();

    public XeroMappingsResponse(){}

    public XeroMappingsResponse(List<XeroAccountView> xeroAccounts, List<AdjustmentType> adjustmentTypes, List<XeroMappingRule> rules, List<TaxRate> taxRatesByCountry) {
        this.accounts = xeroAccounts;
        this.allAdjustmentTypes = adjustmentTypes.stream().map(HostAdjustmentTypeView::new).collect(Collectors.toList());
        this.rules = rules;
        this.allTaxes = taxRatesByCountry.stream().map(HostTaxView::new).collect(Collectors.toList());
    }

    private List<String> createItemTypes() {
        List<String> types = new ArrayList<>();
        for(ItemType itemType : ItemType.values()) {
            types.add(itemType.getName());
        }
        return types;
    }

    public List<XeroAccountView> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<XeroAccountView> accounts) {
        this.accounts = accounts;
    }

    public List<HostAdjustmentTypeView> getAllAdjustmentTypes() {
        return allAdjustmentTypes;
    }

    public void setAllAdjustmentTypes(List<HostAdjustmentTypeView> allAdjustmentTypes) {
        this.allAdjustmentTypes = allAdjustmentTypes;
    }

    public List<HostTaxView> getAllTaxes() {
        return allTaxes;
    }

    public void setAllTaxes(List<HostTaxView> allTaxes) {
        this.allTaxes = allTaxes;
    }

    public List<String> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public List<XeroMappingRule> getRules() {
        return rules;
    }

    public void setRules(List<XeroMappingRule> rules) {
        this.rules = rules;
    }
}
