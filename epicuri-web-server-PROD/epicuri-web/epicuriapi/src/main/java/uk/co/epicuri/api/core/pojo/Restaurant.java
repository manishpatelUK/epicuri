package uk.co.epicuri.api.core.pojo;

/**
 * 26/08/2014
 */
public class Restaurant {
    private int Id;
    private String Name, Description, Email, PhoneNumber, Timezone;
    private Address Address;

    public String getTimezone() {
        return Timezone;
    }

    public void setTimezone(String timezone) {
        Timezone = timezone;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public Restaurant.Address getAddress() {
        return Address;
    }

    public void setAddress(Restaurant.Address address) {
        Address = address;
    }

    public static class Address {
        private String Street, Town, City, PostCode;

        public String getStreet() {
            return Street;
        }

        public void setStreet(String street) {
            Street = street;
        }

        public String getTown() {
            return Town;
        }

        public void setTown(String town) {
            Town = town;
        }

        public String getCity() {
            return City;
        }

        public void setCity(String city) {
            City = city;
        }

        public String getPostCode() {
            return PostCode;
        }

        public void setPostCode(String postCode) {
            PostCode = postCode;
        }
    }
}
