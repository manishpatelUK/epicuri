package uk.co.epicuri.serverapi.common.pojo.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CreditCardData {
    private String ccToken;
    private String digits;
    private String monthExpiry;
    private String yearExpiry;
    private String externalId;

    public String getCcToken() {
        return ccToken;
    }

    public void setCcToken(String ccToken) {
        if(ccToken != null) {
            ccToken = ccToken.trim();
        }
        this.ccToken = ccToken;
    }

    public String getDigits() {
        return digits;
    }

    public void setDigits(String digits) {
        if(digits != null) {
            digits = digits.trim();
        }
        this.digits = digits;
    }

    public String getMonthExpiry() {
        return monthExpiry;
    }

    public void setMonthExpiry(String monthExpiry) {
        this.monthExpiry = monthExpiry;
    }

    public String getYearExpiry() {
        return yearExpiry;
    }

    public void setYearExpiry(String yearExpiry) {
        this.yearExpiry = yearExpiry;
    }

    public boolean equalsExceptToken(CreditCardData creditCardData) {
        return EqualsBuilder.reflectionEquals(this, creditCardData, "ccToken", "externalId");
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
