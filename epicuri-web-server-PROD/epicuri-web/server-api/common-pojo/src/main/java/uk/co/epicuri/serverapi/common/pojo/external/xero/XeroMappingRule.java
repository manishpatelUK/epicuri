package uk.co.epicuri.serverapi.common.pojo.external.xero;

public class XeroMappingRule {
    public static final String NONE = "NONE";
    public static final String ITEM_TYPE = "Item Type";
    public static final String PAYMENT_TYPE = "Payment Type";
    public static final String DISCOUNT_TYPE = "Discount Type";
    public static final String GRATUITY_TYPE = "Gratuity Type";

    private String id;
    private String ruleType;
    private String taxId;
    private String taxName;
    private String typeName;
    private String typeId;
    private String xeroAccountCode;
    private String originalKey;

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getXeroAccountCode() {
        return xeroAccountCode;
    }

    public void setXeroAccountCode(String xeroAccountCode) {
        this.xeroAccountCode = xeroAccountCode;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public void setOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }
}
