package uk.co.epicuri.bookingapi.endpoints.menu;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.bookingapi.endpoints.auth.LogonController;
import uk.co.epicuri.bookingapi.pojo.LogonResponse;
import uk.co.epicuri.bookingapi.pojo.MenuResponse;

public class MenuControllerTest {

    @Test
    public void testItems() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        securitydb.open();

        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);

        MenuController menuController = new MenuController(api,securitydb);
        LogonController logonController = new LogonController(api,securitydb);

        LogonResponse response = logonController.logon("5");
        Assert.assertTrue(response != null);
        Assert.assertTrue(StringUtils.isNotBlank(response.getToken()));

        String token = response.getToken();

        MenuResponse menuResponse = menuController.items("5", token);

        Assert.assertTrue(menuResponse != null);
        Assert.assertTrue(menuResponse.getMenus().size() > 0);
        Assert.assertTrue(menuResponse.getMenus().get(0).getCategories().size() > 0);
        Assert.assertTrue(menuResponse.getMenus().get(0).getCategories().get(0).getGroups().size() > 0);
        Assert.assertTrue(menuResponse.getMenus().get(0).getCategories().get(0).getGroups().get(0).getItems().size() > 0);

        //write the json out
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(menuResponse));
    }
}