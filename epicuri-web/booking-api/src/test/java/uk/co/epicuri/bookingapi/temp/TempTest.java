package uk.co.epicuri.bookingapi.temp;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QTable;
import org.junit.Assert;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Service;
import uk.co.epicuri.api.core.pojo.floor.Floor;
import uk.co.epicuri.api.core.pojo.floor.Layout;
import uk.co.epicuri.api.core.pojo.session.Table;

import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class TempTest {

    @Test
    public void arraytest() throws Exception{
        /*QConnection connection = new QBasicConnection("localhost",5001,null,null);
        connection.open();

        Object[] array = (Object[])connection.sync("(1b;([] a:1 2 3))");
        boolean a = (Boolean)array[0];
        QTable table = (QTable)array[1];

        Assert.assertTrue(a);
        Assert.assertTrue(table.getRowsCount() == 3);*/
    }

    @Test
    public void getTablesForStaging() throws Exception {
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);
        Authentication authentication = api.login("5", "mp", "mp");
        String token = authentication.getAuthKey();
        List<Floor> floorList = api.getFloors(token);
        List<Service> services = api.getServices(token);
        int serviceId = services.get(0).getId();
        int courseId = services.get(0).getCourses().get(0).getId();

        System.out.println("restaurantId,tableName,tableId,serviceId,courseId");
        for(Floor floor : floorList) {
            Layout layout = api.getLayout(floor.getLayout(),token);
            List<Table> tablelIst = layout.getTables();
            for(Table table : tablelIst) {
                System.out.println("5,"+table.getName()+","+table.getId()+","+serviceId+","+courseId);
            }

        }
    }

    @Test
    public void getTablesForProd() throws Exception {
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Authentication authentication = api.login("16", "epicuriadmin", "keshavroshan");
        String token = authentication.getAuthKey();
        List<Floor> floorList = api.getFloors(token);
        List<Service> services = api.getServices(token);
        int serviceId = services.get(0).getId();
        int courseId = services.get(0).getCourses().get(0).getId();

        System.out.println("restaurantId,tableName,tableId,serviceId,courseId");
        for(Floor floor : floorList) {
            Layout layout = api.getLayout(floor.getLayout(),token);
            List<Table> tablelIst = layout.getTables();
            for(Table table : tablelIst) {
                System.out.println("16,"+table.getName()+","+table.getId()+","+serviceId+","+courseId);
            }

        }
    }
}
