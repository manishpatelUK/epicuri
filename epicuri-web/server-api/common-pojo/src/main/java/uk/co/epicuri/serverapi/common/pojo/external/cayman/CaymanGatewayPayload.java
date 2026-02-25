package uk.co.epicuri.serverapi.common.pojo.external.cayman;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {"authorization-code":"OK963C","actiontype":"
 * sale","amount":"12.00","billing":{"address1":"Westbay","city":"GCayman","country"
 * :"KY","email":"thiru@cg.com","first-name":"Thiru","last-name":"
 * ","postal":"49729"},"currency":"USD","industry":"ecommerce","invoiceno":"1239","masked
 * PAN":"4012XXXXXXXX0026","result":"1","result-code":"00","result-text":"Transaction is
 * approved","retRefnum":"021000047708","transaction-id":"51fcfaf6-accf-338a-af03-
 * 1498d9e16c3d"}
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaymanGatewayPayload {
    @JsonProperty("transaction-id")
    private String transactionId;

    @JsonProperty("authorization-code")
    private String authorizationCode;

    @JsonProperty("actiontype")
    private String actionType;

    private String amount;
    private Billing billing;
    private String currency;
    private String industry;

    @JsonProperty("invoiceno")
    private String invoiceNumber;
    private String maskedPAN;
    private String result;

    @JsonProperty("result-code")
    private String resultCode;

    @JsonProperty("result-text")
    private String resultText;

    private String retRefnum;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Billing getBilling() {
        return billing;
    }

    public void setBilling(Billing billing) {
        this.billing = billing;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMaskedPAN() {
        return maskedPAN;
    }

    public void setMaskedPAN(String maskedPAN) {
        this.maskedPAN = maskedPAN;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public String getRetRefnum() {
        return retRefnum;
    }

    public void setRetRefnum(String retRefnum) {
        this.retRefnum = retRefnum;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Billing {
        private String address1;
        private String city;
        private String country;
        private String email;

        @JsonProperty("first-name")
        private String firstName;

        @JsonProperty("last-name")
        private String lastName;

        private String postal;

        public String getAddress1() {
            return address1;
        }

        public void setAddress1(String address1) {
            this.address1 = address1;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPostal() {
            return postal;
        }

        public void setPostal(String postal) {
            this.postal = postal;
        }
    }
}
