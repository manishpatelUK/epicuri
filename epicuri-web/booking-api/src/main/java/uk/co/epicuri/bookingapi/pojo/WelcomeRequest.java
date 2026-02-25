package uk.co.epicuri.bookingapi.pojo;

/**
 * Created by Manish on 17/06/2015.
 */
public class WelcomeRequest {
    private String email;
    private String language = "en";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
