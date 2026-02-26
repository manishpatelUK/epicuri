package uk.co.epicuri.serverapi.management.reflect;

import javafx.scene.control.ListView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.webservice.WebService;

/**
 * Created by manish
 */
public class ListViewBuilder<T extends IDAble> {
    private WebService webService;
    private Class<T> clazz;
    private String restaurantIdRestriction;

    public static <T> ListViewBuilder newInstance() {
        return new ListViewBuilder<>();
    }

    private ListViewBuilder() {

    }

    public ListViewBuilder withWebService(WebService webService) {
        this.webService = webService;
        return this;
    }

    public ListViewBuilder withClass(Class<T> clazz) {
        this.clazz = clazz;
        return this;
    }

    public ListViewBuilder withRestaurantId(String id) {
        this.restaurantIdRestriction = id;
        return this;
    }

    public ListView<ModelWrapper<T>> asList() {
        ReflectListCreator<T> listCreator = new ReflectListCreator<>();
        listCreator.setWebService(webService);
        listCreator.setRestaurantIdRestriction(restaurantIdRestriction);
        return listCreator.createListView(clazz);
    }
}
