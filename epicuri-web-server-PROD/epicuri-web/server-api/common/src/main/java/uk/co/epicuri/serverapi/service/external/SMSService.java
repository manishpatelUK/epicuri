package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.external.textlocal.TextLocalResponse;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.Collections;
import java.util.List;

@Service
public class SMSService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private MasterDataService masterDataService;

    @Value("${txtlocal.key}")
    private String textLocalAPIKey;

    private static final String TEXT_LOCAL_URL = "https://api.txtlocal.com/";
    private static final String TEXT_LOCAL_SEND = "send/";
    private static final HttpHeaders STANDARD_HEADERS = createHeaders();

    public boolean send(String message, String number) {
        return send(message, "Epicuri", Collections.singletonList(number));
    }

    public boolean send(String message, String sender, List<String> numbers) {
        //don't send messages in test
        if(!masterDataService.isProdEnvironment()) {
            LOGGER.debug("Not a prod environment - won't send a text");
            return true;
        }

        String apiKey = "apikey=" + textLocalAPIKey;
        message = "&message=" + message;
        sender = "&sender=" + sender;
        String number = "&numbers=" + concatenate(numbers);

        HttpEntity<String> httpEntity = new HttpEntity<>(apiKey + message + sender + number, STANDARD_HEADERS);

        ResponseEntity<TextLocalResponse> response = restTemplate.exchange(TEXT_LOCAL_URL + TEXT_LOCAL_SEND,
                                                                    HttpMethod.POST,
                                                                    httpEntity,
                                                                    TextLocalResponse.class);
        LOGGER.debug("Response: {}: {}", response.getStatusCode(), response.getBody());

        return response.getStatusCode() == HttpStatus.OK
                && response.getBody().getStatus() != null
                && response.getBody().getStatus().equals("success");
    }

    private String concatenate(List<String> numbers) {
        if(numbers.size() > 10000) {
            throw new IllegalArgumentException("Cannot send to more than 10000 recipients");
        }

        return StringUtils.join(numbers,",");
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
