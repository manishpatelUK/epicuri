package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;

import java.math.BigDecimal;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostTaxView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Rate")
    private double rate;

    public HostTaxView(){}

    public HostTaxView(TaxRate rate){
        this.id = rate.getId();
        this.name = rate.getName();
        this.rate = BigDecimal.valueOf(rate.getRate()).scaleByPowerOfTen(-1).doubleValue();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
