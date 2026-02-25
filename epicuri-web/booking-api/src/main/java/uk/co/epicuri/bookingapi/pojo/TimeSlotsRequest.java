package uk.co.epicuri.bookingapi.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 03/05/2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeSlotsRequest {
    @NotNull
    private List<String> times;
    @NotNull
    private String token;
    @NotNull
    private String date;

    private String language = "en";

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TimeSlotsRequest{" +
                "times=" + times +
                ", token='" + token + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
