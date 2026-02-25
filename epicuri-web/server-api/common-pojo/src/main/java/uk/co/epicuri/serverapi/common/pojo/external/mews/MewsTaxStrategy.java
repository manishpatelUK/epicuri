package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MewsTaxStrategy {
    @JsonProperty("Discriminator")
    private String discriminator;
    @JsonProperty("Value")
    private MewsTaxValue value;

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public MewsTaxValue getValue() {
        return value;
    }

    public void setValue(MewsTaxValue value) {
        this.value = value;
    }
}
