package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class TaxTypeControllerTest extends BaseIT {


    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void testGetTaxTypes() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("TaxType");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostTaxView> taxes = Arrays.asList(response.getBody().as(HostTaxView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(taxRateRepository.findAll().size(), taxes.size());
        testEqual(tax1,taxes.stream().filter(t -> t.getId().equals(tax1.getId())).findFirst().orElse(null));
        testEqual(tax2,taxes.stream().filter(t -> t.getId().equals(tax2.getId())).findFirst().orElse(null));
        testEqual(tax3,taxes.stream().filter(t -> t.getId().equals(tax3.getId())).findFirst().orElse(null));
    }

    private void testEqual(TaxRate taxRate, HostTaxView hostTaxView) {
        assertEquals(taxRate.getId(), hostTaxView.getId());
        assertEquals(taxRate.getName(), hostTaxView.getName());
        assertEquals(taxRate.getRate()/10D, hostTaxView.getRate(), 0.001);
    }
}