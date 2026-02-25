package uk.co.epicuri.serverapi.common.pojo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;

@MgmtPojoModel(useAccessMethods = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address{
    @JsonProperty("Street")
    private String street;

    @JsonProperty("Town")
    private String town;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Postcode")
    private String postcode;

    @MgmtIgnoreField
    @JsonProperty("PostCode") //misspelt in client apps :/
    private String postCode;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPostCode() {
        return postcode; // using other field on purpose
    }

    public void setPostCode(String postCode) {
        this.postcode = postCode;// using other field on purpose
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        if (town != null ? !town.equals(address.town) : address.town != null) return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (postcode != null ? !postcode.equals(address.postcode) : address.postcode != null) return false;
        return postCode != null ? postCode.equals(address.postCode) : address.postCode == null;

    }

    @Override
    public int hashCode() {
        int result = street != null ? street.hashCode() : 0;
        result = 31 * result + (town != null ? town.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (postcode != null ? postcode.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", town='" + town + '\'' +
                ", city='" + city + '\'' +
                ", postcode='" + postcode + '\'' +
                ", postCode='" + postCode + '\'' +
                '}';
    }

    public String prettyToString() {
        StringBuilder builder = new StringBuilder().append("");
        if(street != null) {
            builder.append(street).append(", ");
        }
        if(town != null) {
            builder.append(town).append(", ");
        }
        if(city != null) {
            builder.append(city).append(", ");
        }
        if(postcode != null) {
            builder.append(postcode).append(", ");
        }
        if(postCode != null && postcode == null) {
            builder.append(postCode);
        }

        return builder.toString();
    }
}
