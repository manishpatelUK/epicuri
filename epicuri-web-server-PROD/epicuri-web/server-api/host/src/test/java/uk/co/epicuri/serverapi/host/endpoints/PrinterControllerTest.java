package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterView;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterViewUpdate;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterRedirectRequest;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterRedirectResponse;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.host.util.PrinterUtil;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class PrinterControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        printer1.setRestaurantId(restaurant1.getId());
        printer2.setRestaurantId(restaurant1.getId());

        printerRepository.save(printer1);
        printerRepository.save(printer2);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void testGetPrinters() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Printer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostPrinterView> printers = Arrays.asList(response.getBody().as(HostPrinterView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, printers.size());
        testPrintersEqual(printer1, printers.stream().filter(p -> p.getId().equals(printer1.getId())).findFirst().get());
        testPrintersEqual(printer2, printers.stream().filter(p -> p.getId().equals(printer2.getId())).findFirst().get());

        printer1.setRestaurantId(restaurant2.getId());
        printer2.setRestaurantId(restaurant2.getId());
        printerRepository.save(printer1);
        printerRepository.save(printer2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Printer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        printers = Arrays.asList(response.getBody().as(HostPrinterView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(0, printers.size());
    }

    @Test
    public void testPutMacAddressNegative() throws Exception {
        String token = getTokenForStaff(staff1);

        printer1.setRestaurantId(restaurant1.getId());
        printer2.setRestaurantId(restaurant2.getId());
        printerRepository.save(printer1);
        printerRepository.save(printer2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(new IdPojo("foobar"))
                .put("Printer/macAddress/"+printer2.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPutPrinterInfo() throws Exception {
        String token = getTokenForStaff(staff1);

        printer1.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer1);

        HostPrinterViewUpdate update = new HostPrinterViewUpdate();
        update.setIpAddress("192.168.108.108");
        update.setMacAddress("foobar");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(update)
                .put("Printer/"+printer1.getId());

        Printer printer = printerRepository.findOne(printer1.getId());
        assertEquals("foobar", printer.getMacAddress());
        assertEquals("192.168.108.108", printer.getIp());
    }

    private void testPrintersEqual(Printer printer, HostPrinterView hostPrinterView) {
        assertEquals(printer.getId(), hostPrinterView.getId());
        assertEquals(printer.getIp(), hostPrinterView.getIp());
        assertEquals(printer.getName(), hostPrinterView.getName());
    }

    @Test
    public void testPutRedirect() throws Exception {
        String token = getTokenForStaff(staff1);

        PrinterRedirectRequest redirectRequest = new PrinterRedirectRequest();
        redirectRequest.setFrom(printer1.getId());
        redirectRequest.setTo(printer1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        redirectRequest.setTo(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        redirectRequest.setFrom(null);
        redirectRequest.setTo(printer2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        redirectRequest.setFrom(printer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(printerRepository.findOne(printer1.getId()).getRedirect(), PrinterUtil.redirectId(printer1.getId(), printer2.getId()));

        redirectRequest.setFrom(printer2.getId());
        redirectRequest.setTo(printer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        printer3.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer3);

        redirectRequest.setFrom(printer2.getId());
        redirectRequest.setTo(printer3.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(printerRepository.findOne(printer2.getId()).getRedirect(), PrinterUtil.redirectId(printer2.getId(), printer3.getId()));

        redirectRequest.setFrom(printer3.getId());
        redirectRequest.setTo(printer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        redirectRequest.setFrom(printer3.getId());
        redirectRequest.setTo(printer2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(redirectRequest)
                .put("Printer/Redirect");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testGetRedirects() throws Exception {
        printer1.setRedirect(PrinterUtil.redirectId(printer1, printer2));
        printer2.setRedirect(PrinterUtil.redirectId(printer2, printer3));
        printer3.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer1);
        printerRepository.save(printer2);
        printerRepository.save(printer3);

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Printer/RedirectedPrinters");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterRedirectResponse> redirects = Arrays.asList(response.getBody().as(PrinterRedirectResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, redirects.size());
        PrinterRedirectResponse response1 = redirects.stream().filter(r -> r.getFrom().getId().equals(printer1.getId())).findFirst().get();
        assertEquals(printer2.getId(), response1.getTo().getId());
        assertEquals(printer1.getId(), response1.getFrom().getId());
        assertEquals(PrinterUtil.redirectId(printer1, printer2), response1.getId());
        PrinterRedirectResponse response2 = redirects.stream().filter(r -> r.getFrom().getId().equals(printer2.getId())).findFirst().get();
        assertEquals(printer3.getId(), response2.getTo().getId());
        assertEquals(printer2.getId(), response2.getFrom().getId());
        assertEquals(PrinterUtil.redirectId(printer2, printer3), response2.getId());
    }

    @Test
    public void testDeleteRedirect() throws Exception {
        printer1.setRedirect(PrinterUtil.redirectId(printer1, printer2));
        printer2.setRedirect(PrinterUtil.redirectId(printer2, printer3));
        printerRepository.save(printer1);
        printerRepository.save(printer2);

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "duddid")
                .delete("Printer/Redirect/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(printer1, printerRepository.findOne(printer1.getId()));
        assertEquals(printer2, printerRepository.findOne(printer2.getId()));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", PrinterUtil.redirectId(printer1, printer2))
                .delete("Printer/Redirect/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNull(printerRepository.findOne(printer1.getId()).getRedirect());
        assertNotNull(printerRepository.findOne(printer2.getId()).getRedirect());
    }
}