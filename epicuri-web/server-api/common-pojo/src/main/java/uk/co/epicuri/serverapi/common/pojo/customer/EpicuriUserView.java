package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EpicuriUserView {
    @JsonProperty("Name")
    private Name name;

    public EpicuriUserView(){}
    public EpicuriUserView(Customer customer){
        name = new Name(customer);
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public class Name {
        @JsonProperty("Firstname")
        private String firstName;

        @JsonProperty("Surname")
        private String lastName;

        public Name(){}
        public Name(Customer customer) {
            firstName = customer.getFirstName();
            lastName = customer.getLastName();
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
    }
}
