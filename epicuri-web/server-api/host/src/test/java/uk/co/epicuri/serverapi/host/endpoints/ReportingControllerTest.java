package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReports;
import uk.co.epicuri.serverapi.service.external.PaymentSenseReportingUtilTest;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;
import uk.co.epicuri.serverapi.service.reporting.BaseReportingServiceTest;
import uk.co.epicuri.serverapi.service.reporting.ReportingService;

import java.util.ArrayList;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.co.epicuri.serverapi.spring.CsvMessageConverter.TEXT_CSV;

/**
 * Created by manish.
 */
public class ReportingControllerTest extends BaseReportingServiceTest  {

    @Autowired
    private ReportingService reportingService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetCSV() throws Exception {
        Response response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/customerDetails");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/itemsAggregated");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/payments");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/itemDetails");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/revenues");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/reservations");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("/Reporting/takeaways");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testGetCSVWithTime() throws Exception {
        Response response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017 09:00")
                .queryParam("end", "28-06-2017 12:00")
                .get("/Reporting/customerDetails");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        //24h clock
        response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017 09:00")
                .queryParam("end", "28-06-2017 19:00")
                .get("/Reporting/customerDetails");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

    }

    @Test
    public void testDataExists() {
        Response response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "28-06-2017")
                .queryParam("end", "29-06-2017")
                .get("/Reporting/itemsAggregated");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        String stringResponse = new String(response.getBody().asByteArray());
        assertTrue(stringResponse.contains("DRINK"));
    }

    /*@Test
    public void testCommas() {
        menuItem1.setName("Foo,bar");
        menuItemRepository.save(menuItem1);

        Response response = given()
                .accept(TEXT_CSV)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "28-06-2017")
                .queryParam("end", "29-06-2017")
                .get("/Reporting/itemsAggregated");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        String stringResponse = new String(response.getBody().asByteArray());
        assertTrue(stringResponse.contains("DRINK"));
    }*/


}
