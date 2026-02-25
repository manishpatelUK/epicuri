package uk.co.epicuri.serverapi.common.pojo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayName;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtExternalId;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.db.TableNames;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = TableNames.TAX_RATES)
public class TaxRate extends Deletable {
    @MgmtIgnoreField
    public static TaxRate ZERO = new TaxRate(0);

    @MgmtExternalId(externalClass = Country.class, endpoint = "uk.co.epicuri.serverapi.common.pojo.model.Country", restrictOnParentId = false)
    private String countryId; //no need for index as this table is tiny

    @MgmtDisplayField
    private String name;

    @MgmtDisplayName(name = "Tax Rate (%) * 10")
    @Min(0)//0%
    @Max(1000) //100%
    private int rate = 0;

    public TaxRate(){}

    public TaxRate(int rate){
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public double getRateAsDouble() {
        return getRateAsDouble(rate);
    }

    public static double getRateAsDouble(int rate) {
        return BigDecimal.valueOf(rate).scaleByPowerOfTen(-3).doubleValue();
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}
