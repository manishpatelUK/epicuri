package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.BasicBusinessIntelligenceReport;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;
import uk.co.epicuri.serverapi.engines.AggregatorTestBase;
import uk.co.epicuri.serverapi.engines.BasicBIAggregator;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

/**
 * Created by manish.
 */
public class BusinessIntelligenceControllerTest extends AggregatorTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void testGetBasicMetricPopularItems1() throws Exception {
        String token = getTokenForStaff(staff1);

        BasicBusinessIntelligenceReport report = getBasicBusinessIntelligenceReport(token);

        final Map<String, List<BasicBusinessIntelligenceReport.PopularItem>> popularItemsReport = report.getPopularItemsReport();
        assertEquals(1, popularItemsReport.get(ItemType.DRINK.getName()).size());
        assertEquals(1, popularItemsReport.get(ItemType.FOOD.getName()).size());
        assertEquals(1, popularItemsReport.get(ItemType.OTHER.getName()).size());
    }

    @Test
    public void testGetBasicMetricPopularItems2() throws Exception {
        String token = getTokenForStaff(staff1);

        menuItem2.setType(ItemType.FOOD);
        menuItem3.setType(ItemType.FOOD);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        BasicBusinessIntelligenceReport report = getBasicBusinessIntelligenceReport(token);

        final Map<String, List<BasicBusinessIntelligenceReport.PopularItem>> popularItemsReport = report.getPopularItemsReport();
        assertEquals(0, popularItemsReport.get(ItemType.DRINK.getName()).size());
        assertEquals(3, popularItemsReport.get(ItemType.FOOD.getName()).size());
        assertEquals(0, popularItemsReport.get(ItemType.OTHER.getName()).size());

        assertEquals(menuItem1.getName(), popularItemsReport.get(ItemType.FOOD.getName()).get(0).getName());
    }

    @Test
    public void testGetBasicMetricAverageItems1() throws Exception {
        String token = getTokenForStaff(staff1);

        BasicBusinessIntelligenceReport report = getBasicBusinessIntelligenceReport(token);

        final Map<String, Map<String, Double>> averageItemsReport = report.getAverageItemsReport();
        // monday - 1 food item, 1 drink item over 2 Mondays - so 0.5 each
        assertEquals(0.5D, averageItemsReport.get(BasicBIAggregator.DAYS[0]).get(ItemType.FOOD.getName()), 0.001D);
        assertEquals(0.5D, averageItemsReport.get(BasicBIAggregator.DAYS[0]).get(ItemType.DRINK.getName()), 0.001D);
        assertEquals(0D, averageItemsReport.get(BasicBIAggregator.DAYS[0]).get(ItemType.OTHER.getName()), 0.001D);
    }

    @Test
    public void testGetBasicMetricDailySessions1() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpMixedSessions();

        BasicBusinessIntelligenceReport report = getBasicBusinessIntelligenceReport(token);
        final Map<String, Map<String, Double>> averageSessionsReport = report.getAverageSessionsReport();

        // monday - 1 seated, 1 delivery -- 2 mondays in total
        // thursday - 1 collection -- 1 thursdays in total
        assertEquals(0.5, averageSessionsReport.get(BasicBIAggregator.DAYS[0]).get(BasicBIAggregator.SEATED), 0.01);
        assertEquals(0.5, averageSessionsReport.get(BasicBIAggregator.DAYS[0]).get(BasicBIAggregator.TAKEAWAY_DELIVERY), 0.01);
        assertEquals(1, averageSessionsReport.get(BasicBIAggregator.DAYS[3]).get(BasicBIAggregator.TAKEAWAY_COLLECTION), 0.01);
    }

    private void setUpMixedSessions() {
        session1.setSessionType(SessionType.SEATED);
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.COLLECTION);
        session3.setSessionType(SessionType.TAKEAWAY);
        session3.setTakeawayType(TakeawayType.DELIVERY);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
    }

    @Test
    public void testGetBasicReportWhenSessionsArchived() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpMixedSessions();

        session1.setClosedTime(session1.getStartTime()+1);
        sessionRepository.save(session1);
        sessionService.clearWithSession(session1, true, true, true, true, true);

        BasicBusinessIntelligenceReport report = getBasicBusinessIntelligenceReport(token);
        final Map<String, Map<String, Double>> averageSessionsReport = report.getAverageSessionsReport();

        // monday - 1 seated, 1 delivery -- 2 mondays in total
        // thursday - 1 collection -- 1 thursdays in total
        assertEquals(0.5, averageSessionsReport.get(BasicBIAggregator.DAYS[0]).get(BasicBIAggregator.SEATED), 0.01);
        assertEquals(0.5, averageSessionsReport.get(BasicBIAggregator.DAYS[0]).get(BasicBIAggregator.TAKEAWAY_DELIVERY), 0.01);
        assertEquals(1, averageSessionsReport.get(BasicBIAggregator.DAYS[3]).get(BasicBIAggregator.TAKEAWAY_COLLECTION), 0.01);
    }

    private BasicBusinessIntelligenceReport getBasicBusinessIntelligenceReport(String token) {

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.ALL_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("start", "19-06-2017")
                .queryParam("end", "28-06-2017")
                .get("BusinessIntelligence/Basic");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        return response.as(BasicBusinessIntelligenceReport.class, ObjectMapperType.JACKSON_2);
    }
}
