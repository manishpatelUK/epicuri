package uk.co.epicuri.serverapi.management.reflect;

import javafx.scene.control.ListView;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.ui.ReflectionView;
import uk.co.epicuri.serverapi.management.webservice.WebService;

/**
 * Created by manish
 */
public class PojoViewBuilder<T> {

    private WebService webService;
    private Class<T> clazz;
    private T instance;

    public static PojoViewBuilder newInstance() {
        return new PojoViewBuilder();
    }

    private PojoViewBuilder() {

    }

    public PojoViewBuilder withWebService(WebService webService) {
        this.webService = webService;
        return this;
    }

    public PojoViewBuilder withClass(Class<T> clazz) {
        this.clazz = clazz;
        return this;
    }

    public PojoViewBuilder withInstance(T instance) {
        this.instance = instance;
        return this;
    }

    public ReflectionView asView() {
        ReflectViewCreator<T> viewCreator = new ReflectViewCreator<>();
        viewCreator.setWebService(webService);
        return viewCreator.createViewNode(clazz, instance);
    }

    public ReflectionView asView(ParameterisedSimpleAction<String> action) {
        ReflectViewCreator<T> viewCreator = new ReflectViewCreator<>();
        viewCreator.setWebService(webService);
        return viewCreator.createStringViewNode((String)instance, action);
    }
}
