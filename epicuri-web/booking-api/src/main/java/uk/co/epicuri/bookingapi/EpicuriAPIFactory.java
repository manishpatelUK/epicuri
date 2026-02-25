package uk.co.epicuri.bookingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import uk.co.epicuri.api.core.EpicuriAPI;

/**
 * 30/04/2015
 */
public class EpicuriAPIFactory {

    private String apiEnvironment = "STAGING";
    private String apiVersion = "1";

    @JsonProperty
    public String getApiEnvironment() {
        return apiEnvironment;
    }

    @JsonProperty
    public void setApiEnvironment(String apiEnvironment) {
        this.apiEnvironment = apiEnvironment;
    }

    @JsonProperty
    public String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public EpicuriAPI build(Environment environment) {

        EpicuriAPI.Environment requestedEnvironment = EpicuriAPI.Environment.STAGING;
        if(apiEnvironment.equalsIgnoreCase("prod")) {
            requestedEnvironment = EpicuriAPI.Environment.PROD;
        }
        else if(apiEnvironment.equalsIgnoreCase("dev")) {
            requestedEnvironment = EpicuriAPI.Environment.DEV;
        }

        final EpicuriAPI api = new EpicuriAPI(requestedEnvironment, apiVersion);
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {

            }

            @Override
            public void stop() throws Exception {
                //todo clean up
            }
        });
        return api;
    }
}
