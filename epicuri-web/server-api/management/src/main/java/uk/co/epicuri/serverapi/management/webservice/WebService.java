package uk.co.epicuri.serverapi.management.webservice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.StaffAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.management.FieldEdit;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.management.SSLUtil;
import uk.co.epicuri.serverapi.management.event.SimpleAction;
import uk.co.epicuri.serverapi.management.controllers.Blackboard;
import uk.co.epicuri.serverapi.management.controllers.Refreshable;
import uk.co.epicuri.serverapi.management.external.PostCodeLookup;
import uk.co.epicuri.serverapi.management.model.Environment;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by manish
 */
public class WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebService.class);

    private String baseURL;
    private static WebService webService;

    private Environment selectedEnvironment;
    private Map<Environment,String> tokens = new HashMap<>();

    private RestTemplate restTemplate;
    public static final Map<String,?> NULL_QUERY_MAP = Collections.unmodifiableMap(new HashMap<>());

    private SimpleAction onAPIStartedAction;
    private SimpleAction onAPIEndedAction;

    public static synchronized WebService getWebService() {
        if(webService == null) {
            webService = new WebService();
        }
        return webService;
    }

    public synchronized void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public Environment getSelectedEnvironment() {
        return selectedEnvironment;
    }

    public boolean isValid() {
        return selectedEnvironment != null && selectedEnvironment != Environment.NONE && tokens.get(selectedEnvironment) != null;
    }

    public void setSelectedEnvironment(Environment selectedEnvironment) {
        this.selectedEnvironment = selectedEnvironment;
        if(selectedEnvironment != null) {
            setBaseURL(selectedEnvironment.getUrl());

            // renew the template
            if(selectedEnvironment == Environment.PROD) {
                try {
                    SSLUtil.turnOffSslChecking();
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }
            restTemplate = new RestTemplate();
        }
    }

    public void updateToken(String token) {
        tokens.put(selectedEnvironment, token);
    }

    public void doLogin(String username, String password) {
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return;
        }

        apiCallPre();

        try {
            String endpoint = prepareEndPoint(Endpoints.LOGIN);
            HttpHeaders headers = getStandardHeaders(tokens.get(selectedEnvironment));
            headers.add(Params.ADMIN_U, username);
            headers.add(Params.AUTHORIZATION, password);
            HttpEntity entity = new HttpEntity<>("parameters", headers);
            long timer = System.currentTimeMillis();
            IdPojo idPojo = restTemplate.exchange(endpoint, HttpMethod.POST, entity, IdPojo.class, NULL_QUERY_MAP).getBody();
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            if(idPojo != null && idPojo.getId() != null) {
                updateToken(idPojo.getId());
            } else {
                throw new RestClientException("Return object not obtained");
            }

            Blackboard.get().forEach(Refreshable::refresh);

        } catch (RestClientException ex){
            LOGGER.error("Web service error", ex);
            Alert alert = showAlert(ex);

            alert.show();
        } finally {
            apiCallPost();
        }
    }

    public StaffView doLogin(String restaurantId, String username, String password) {
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return null;
        }

        apiCallPre();

        StaffView staffView = null;
        try {
            String endpoint = prepareEndPoint(Endpoints.LOGIN_PATH);
            HttpHeaders headers = getStandardHeaders(tokens.get(selectedEnvironment));
            headers.add(Params.AUTHORIZATION, password);
            StaffAuthPayload staffAuthPayload = new StaffAuthPayload();
            staffAuthPayload.setPassword(password);
            staffAuthPayload.setRestaurantId(restaurantId);
            staffAuthPayload.setUsername(username);

            HttpEntity entity = new HttpEntity<>(staffAuthPayload, headers);
            long timer = System.currentTimeMillis();
            staffView = restTemplate.exchange(endpoint, HttpMethod.POST, entity, StaffView.class, NULL_QUERY_MAP).getBody();
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            if(staffView != null && staffView.getAuthKey() != null) {
                updateToken(staffView.getAuthKey());
            } else {
                throw new RestClientException("Return object not obtained");
            }

            Blackboard.get().forEach(Refreshable::refresh);

        } catch (RestClientException ex){
            LOGGER.error("Web service error", ex);
            Alert alert = showAlert(ex);

            alert.show();
        } finally {
            apiCallPost();
        }

        return staffView;
    }

    private Alert showAlert(RestClientException ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Web Service Error");
        alert.setHeaderText("Error calling server API");
        alert.setContentText("There was an error calling the web service. This could be an application error or the server could be down.");

        TextArea textArea = new TextArea(ExceptionUtils.getStackTrace(ex));
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("The exception stacktrace was:"), 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        return alert;
    }

    public <T> T getEntity(String entity, String id, Class<T> clazz) {
        return get(Endpoints.MANAGEMENT + "/" + entity + "/" + id, clazz);
    }

    public synchronized <O> O get(String endpoint, Class<O> outputClass) throws RestClientException{
        return get(endpoint, outputClass, NULL_QUERY_MAP);
    }

    public synchronized <O> O get(String endpoint, Class<O> outputClass, Map<String,?> queryParameters) throws RestClientException{
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return null;
        }

        apiCallPre();

        long timer = System.currentTimeMillis();
        try {
            endpoint = prepareEndPoint(endpoint);
            HttpEntity entity = new HttpEntity<>("parameters", getStandardHeaders(tokens.get(selectedEnvironment)));
            return restTemplate.exchange(endpoint, HttpMethod.GET, entity, outputClass, queryParameters).getBody();
        } catch(RestClientException ex) {
            showAlert(ex);
            throw ex;
        } finally {
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            apiCallPost();
        }
    }

    public synchronized <O> List<O> getAsList(String endpoint, Class<O> outputClass) throws RestClientException{
        return getAsList(endpoint, outputClass, NULL_QUERY_MAP);
    }

    public synchronized <O> List<O> getAsList(String endpoint, Class<O> outputClass, Map<String,?> queryParameters) throws RestClientException{
        List objects = get(endpoint, List.class, queryParameters);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<O> list = new ArrayList<>();
        if(objects == null) {
            return list;
        }

        for(Object object : objects) {
            list.add(mapper.convertValue(object, outputClass));
        }
        return list;
    }

    public void putSingleFieldUpdate(String entity, String id, String fieldName, Object newValue) {
        FieldEdit fieldEdit = new FieldEdit();
        fieldEdit.setFieldName(fieldName);
        fieldEdit.setEditedObject(newValue);
        fieldEdit.setNullify(newValue == null);
        put(Endpoints.FIELD_EDIT + "/" + entity + "/" + id, fieldEdit);
    }

    public synchronized <I> void put(String endpoint, I input) throws RestClientException{
        put(endpoint, input, NULL_QUERY_MAP);
    }

    public synchronized <I> void put(String endpoint, I input, Map<String,?> queryParameters) throws RestClientException{
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return;
        }

        apiCallPre();

        long timer = System.currentTimeMillis();
        try {
            endpoint = prepareEndPoint(endpoint);
            HttpEntity<I> entity = new HttpEntity<>(input, getStandardHeaders(tokens.get(selectedEnvironment)));
            restTemplate.exchange(endpoint, HttpMethod.PUT, entity, Object.class, queryParameters);
        } catch(RestClientException ex) {
            showAlert(ex);
            throw ex;
        } finally {
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            apiCallPost();
        }
    }

    public synchronized <I,O> O post(String endpoint, I input, Class<O> output) throws RestClientException{
        return post(endpoint, input, output, NULL_QUERY_MAP, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
    }

    public synchronized <I,O> O post(String endpoint, I input, Class<O> output, Map<String,?> queryParameters, MediaType acceptMediaType, MediaType contentMediaType) throws RestClientException{
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return null;
        }

        apiCallPre();

        long timer = System.currentTimeMillis();
        try {
            endpoint = prepareEndPoint(endpoint);
            HttpEntity<I> entity = new HttpEntity<>(input, getStandardHeaders(tokens.get(selectedEnvironment), acceptMediaType, contentMediaType));
            return restTemplate.exchange(endpoint, HttpMethod.POST, entity, output, queryParameters).getBody();
        } catch(RestClientException ex) {
            ex.printStackTrace();
            showAlert(ex);
            throw ex;
        } finally {
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            apiCallPost();
        }
    }

    public synchronized void delete(String endpoint) throws RestClientException{
        delete(endpoint, NULL_QUERY_MAP);
    }

    public synchronized void delete(String endpoint, Map<String,?> queryParameters) throws RestClientException{
        if(selectedEnvironment == null || selectedEnvironment == Environment.NONE) {
            return;
        }

        apiCallPre();

        long timer = System.currentTimeMillis();
        try {
            endpoint = prepareEndPoint(endpoint);
            HttpEntity entity = new HttpEntity<>(getStandardHeaders(tokens.get(selectedEnvironment)));
            restTemplate.exchange(endpoint, HttpMethod.DELETE, entity, Object.class, queryParameters);
        } catch(RestClientException ex) {
            showAlert(ex);
            throw ex;
        }  finally {
            LOGGER.debug("API call took {} ms to complete", System.currentTimeMillis()-timer);
            apiCallPost();
        }
    }

    private HttpHeaders getStandardHeaders(String token, MediaType acceptMediaType, MediaType contentMediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "EpicuriWaiter/0");
        headers.set("X-Epicuri-API-Version", "3");
        headers.setAccept(Collections.singletonList(acceptMediaType));
        headers.setContentType(contentMediaType);
        if(token != null) {
            headers.set("Authorization", token);
        }

        return headers;
    }

    private HttpHeaders getStandardHeaders(String token) {
        return getStandardHeaders(token, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
    }

    private void apiCallPre() {
        if(onAPIStartedAction != null) {
            onAPIStartedAction.onAction();
        }
    }

    private void apiCallPost() {
        if(onAPIEndedAction != null) {
            onAPIEndedAction.onAction();
        }
    }

    private String prepareEndPoint(String endpoint) {
        if(!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        endpoint = baseURL + endpoint;

        return endpoint;
    }

    public LatLongPair postCodeLookup(String postCode) {
        RestTemplate postCodeRestTemplate = new RestTemplate();
        ResponseEntity<PostCodeLookup> lookup = postCodeRestTemplate.getForEntity("https://api.postcodes.io/postcodes/" + postCode, PostCodeLookup.class);
        if(lookup.getStatusCode() == HttpStatus.NOT_FOUND) {
            return null;
        }

        PostCodeLookup.Result result = lookup.getBody().getResult();
        return new LatLongPair(result.getLatitude(), result.getLongitude());
    }

    public void setAPICallStarted(SimpleAction action) {
        onAPIStartedAction = action;
    }

    public void setAPICallEnded(SimpleAction action) {
        onAPIEndedAction = action;
    }
}
