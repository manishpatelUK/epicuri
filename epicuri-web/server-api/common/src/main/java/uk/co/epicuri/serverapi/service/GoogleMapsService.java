package uk.co.epicuri.serverapi.service;

import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.Address;

import javax.annotation.PostConstruct;

/**
 * Created by manish.
 */
@Service
public class GoogleMapsService {
    //https://developers.google.com/maps/documentation/distance-matrix/intro

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleMapsService.class);

    private static final Double METERS_TO_MILES_RATE = 0.000621371;
    private static final String GOOGLE_API_DISTANCE_DELIMITER = ".";
    private static final String GOOGLE_API_GEOLOCATION_DELIMITER = ",";

    private GeoApiContext context ;

    @Value("${google.maps.key}")
    private String API_KEY;

    @PostConstruct
    public void init(){
        this.context = new GeoApiContext().setApiKey(API_KEY);
    }

    public double getDistanceMiles(Address address1, Address address2) {
        LOGGER.debug("Getting distance between address {} and address {}", address1, address2);
        // If one of addresses is null return default value
        if(address1 == null || address2 == null) {
            return Double.MAX_VALUE;
        }

        try {
            DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context);

            DistanceMatrix trix = req.origins(this.buildAddress(address1, GOOGLE_API_DISTANCE_DELIMITER))
                    .destinations(this.buildAddress(address2, GOOGLE_API_DISTANCE_DELIMITER))
                    .mode(TravelMode.DRIVING)
                    .units(Unit.IMPERIAL)
                    .await();

            // If not found google returns empty array as a rows
            if(trix.rows[0].elements.length < 1 || trix.rows[0].elements[0].status != DistanceMatrixElementStatus.OK){
                LOGGER.error("There is no route between {} and {}", address1, address2);
                return Double.MAX_VALUE;
            }

            return trix.rows[0].elements[0].distance.inMeters * METERS_TO_MILES_RATE;
        } catch (ApiException e) {
            LOGGER.error("Api exception while communication with google api : {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Java exception while communication with google api : {}", e.getMessage());
        }
        return Double.MAX_VALUE;
    }

    public boolean isAddressValid(Address address) {
        LOGGER.debug("Checking address {} is valid.", address);

        if(StringUtils.isBlank(address.getStreet()) && StringUtils.isBlank(address.getPostcode())) {
            return false;
        }

        try {
            GeocodingApiRequest req = GeocodingApi.geocode(this.context,
                    this.buildAddress(address, GOOGLE_API_GEOLOCATION_DELIMITER));

            GeocodingResult[] res = req.await();

            if (res.length > 0)
                return true;

        } catch (ApiException e) {
            LOGGER.error("Api exception while communication with google api : {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Java exception while communication with google api : {}", e.getMessage());
        }

        return false;
    }

    private String buildAddress(Address a, String delimiter){
        StringBuilder retVal = new StringBuilder(150);
        if(!StringUtils.isBlank(a.getStreet())){
            retVal.append(a.getStreet() + delimiter);
        }
        if(!StringUtils.isBlank(a.getTown())){
            retVal.append(a.getTown() + delimiter);
        }
        if(!StringUtils.isBlank(a.getCity())){
            retVal.append(a.getCity() + delimiter);
        }
        if(!StringUtils.isBlank(a.getPostCode())){
            retVal.append(a.getPostCode());
        }else if(!StringUtils.isBlank(a.getPostcode())){
            retVal.append(a.getPostcode());
        }

        LOGGER.debug("Building address {} to String {}", a, retVal.toString());
        return retVal.toString();
    }
}
