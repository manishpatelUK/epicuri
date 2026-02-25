package uk.co.epicuri.serverapi.management.reflect;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Created by manish
 */
public class ReflectListCreator<T extends IDAble> {

    private WebService webService;
    private String restaurantIdRestriction;

    ReflectListCreator() {

    }

    public void setWebService(WebService webService) {
        this.webService = webService;
    }

    public ListView<ModelWrapper<T>> createListView(Class<T> clazz) {
        List<T> list = webService.getAsList(Endpoints.MANAGEMENT +"/"+clazz.getCanonicalName(), clazz);
        if(restaurantIdRestriction != null) {
            Field field = null;
            try {
                field = clazz.getDeclaredField("restaurantId");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            if(field != null) {
                field.setAccessible(true);
                Iterator<T> iterator = list.iterator();
                while (iterator.hasNext()) {
                    T t = iterator.next();
                    try {
                        Object value = field.get(t);
                        if(!restaurantIdRestriction.equals(value)) {
                            iterator.remove();
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ListView<ModelWrapper<T>> view = new ListView<>(ReflectUICreator.getListModel(list));
        view.setCellFactory(c -> new ListCell<ModelWrapper<T>>() {
            @Override
            public void updateItem(ModelWrapper<T> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayString());
                }
            }
        });

        return view;
    }

    public void setRestaurantIdRestriction(String restaurantIdRestriction) {
        this.restaurantIdRestriction = restaurantIdRestriction;
    }
}
