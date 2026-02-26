package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.external.getaddress.GetAddressResponse;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.service.ExternalService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PostcodeLookupService extends ExternalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostcodeLookupService.class);

    @Value("${getaddress.api.key}")
    private String API_KEY;

    private final String getAddressURL = "https://api.getAddress.io/find/";
    private RestTemplate restTemplate = new RestTemplate();

    public List<Address> lookup(String postcode) {
        postcode = postcode.toUpperCase().replaceAll("\\s","");
        HttpHeaders headers = createStandardJsonHeaders();

        LOGGER.trace("Try to do a postcode lookup from getaddress.io with key {}", API_KEY);
        headers.set("Authorization", getBasicAuthentication(API_KEY,API_KEY));

        ResponseEntity<GetAddressResponse> getAddressResponse = restTemplate.exchange(getAddressURL + postcode, HttpMethod.GET, new HttpEntity<>(headers), GetAddressResponse.class);
        if(getAddressResponse.getStatusCode() == HttpStatus.OK) {
            GetAddressResponse response = getAddressResponse.getBody();
            List<Address> addresses = new ArrayList<>();
            for(String string : response.getAddresses()) {
                Address address = parseAddress(postcode, string);
                addresses.add(address);
            }
            return addresses;
        } else {
            return new ArrayList<>();
        }
    }

    public Address parseAddress(String postcode, String string) {
        Address address = new Address();
        String[] bits = string.split(",");
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < bits.length; i++) {
            if(i < 3 && StringUtils.isNotBlank(bits[i])) {
                builder.append(bits[i].trim());
                if(StringUtils.isNotBlank(bits[i+1])) {
                    builder.append(", ");
                }
            } else if(i == 3 && StringUtils.isNotBlank(bits[i])) {
                builder.append(bits[i].trim());
            }
        }
        address.setStreet(builder.toString());

        if(StringUtils.isNotBlank(bits[4])) {
            address.setTown(bits[4].trim());
        }
        if(StringUtils.isNotBlank(bits[5])) {
            address.setCity(bits[5].trim());
        }
        address.setPostcode(postcode);
        return address;
    }
}
