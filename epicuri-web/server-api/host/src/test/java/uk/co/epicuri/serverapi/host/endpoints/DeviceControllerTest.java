package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.AndroidLogPojo;
import uk.co.epicuri.serverapi.common.pojo.host.DeviceRequest;
import uk.co.epicuri.serverapi.common.pojo.model.DeviceDetail;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;
import uk.co.epicuri.serverapi.service.util.SupportService;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 14.7.16..
 */
public class DeviceControllerTest extends BaseIT{

    @Autowired
    private SupportService supportService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void postUpdateDevice() throws Exception {
        Response response;
        String token = "";

        DeviceRequest request = new DeviceRequest();
        request.setRestaurantId(restaurant1.getId());
        request.setAutoUpdating(true);
        request.setHash("23");
        request.setLanguageSetting("engloo");
        request.setNote("note");
        request.setOs("ozzy");
        request.setTimezoneSetting("zony");
        request.setWaiterAppVersion("1");
        request.setWaiterAppVersionId("232");


        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("device");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //OK if staff member exists in that restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("device");

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());
        DeviceDetail deviceDetail = deviceDetailsRepository.findAll().get(0);
        assertEqual(request, deviceDetail);
    }

    private void assertEqual(DeviceRequest deviceRequest, DeviceDetail deviceDetail) {
        assertEquals(deviceRequest.getRestaurantId(), deviceDetail.getRestaurantId());
        assertEquals(deviceRequest.isAutoUpdating(), deviceDetail.isAutoUpdating());
        assertEquals(deviceRequest.getHash(), deviceDetail.getHash());
        assertEquals(deviceRequest.getLanguageSetting(), deviceDetail.getLanguageSetting());
        assertEquals(deviceRequest.getNote(), deviceDetail.getNote());
        assertEquals(deviceRequest.getOs(), deviceDetail.getOs());
        assertEquals(deviceRequest.getTimezoneSetting(), deviceDetail.getTimezoneSetting());
        assertEquals(deviceRequest.getWaiterAppVersion(), deviceDetail.getWaiterAppVersion());
        assertEquals(deviceRequest.getWaiterAppVersionId(), deviceDetail.getWaiterAppVersionId());
    }

    @Test
    public void postLogFiles() throws Exception {
        AsyncCommunicationsService asyncCommunicationsService = mock(AsyncCommunicationsService.class);
        Whitebox.setInternalState(supportService, "asyncCommunicationsService", asyncCommunicationsService);
        expect(asyncCommunicationsService.sendInternalSupportEmail(anyString(),anyString(), anyString())).andReturn(null);
        replay(asyncCommunicationsService);

        AndroidLogPojo androidLogPojo = new AndroidLogPojo();
        androidLogPojo.getLogs().add("foo");
        androidLogPojo.getLogs().add("bar");

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(androidLogPojo)
                .post("device/logs");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        verify(asyncCommunicationsService);
    }
}