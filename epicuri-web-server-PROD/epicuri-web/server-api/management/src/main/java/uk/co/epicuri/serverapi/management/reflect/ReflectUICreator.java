package uk.co.epicuri.serverapi.management.reflect;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import javafx.util.converter.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.management.*;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Floor;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.ui.FileChooserPopup;
import uk.co.epicuri.serverapi.management.ui.GenericFormPopup;
import uk.co.epicuri.serverapi.management.ui.MappedGridPane;
import uk.co.epicuri.serverapi.management.ui.ReflectionView;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Manish Patel
 */
public class ReflectUICreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUICreator.class);

    public static final Map<Class<?>, StringConverter> PRIMITIVE_CONVERTER_MAP = createPrimitiveConverterMap();
    public static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = createPrimitiveMap();

    private static final Map<String,ObservableList<String>> internalListMap = new HashMap<>();


    private static Map<Class<?>, StringConverter> createPrimitiveConverterMap() {
        Map<Class<?>, StringConverter> map = new HashMap<>();
        map.put(Character.TYPE, new CharacterStringConverter());
        map.put(char.class, new CharacterStringConverter());
        map.put(Byte.TYPE, new ByteStringConverter());
        map.put(byte.class, new ByteStringConverter());
        map.put(Short.TYPE, new ShortStringConverter());
        map.put(short.class, new ShortStringConverter());
        map.put(Integer.TYPE, new IntegerStringConverter());
        map.put(int.class, new IntegerStringConverter());
        map.put(Long.TYPE, new LongStringConverter());
        map.put(long.class, new LongStringConverter());
        map.put(Float.TYPE, new FloatStringConverter());
        map.put(float.class, new FloatStringConverter());
        map.put(Double.TYPE, new DoubleStringConverter());
        map.put(double.class, new DoubleStringConverter());

        return map;
    }

    private static Map<Class<?>, Class<?>> createPrimitiveMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(Character.TYPE, char.class);
        map.put(Character.class, char.class);
        map.put(Byte.TYPE, byte.class);
        map.put(Byte.class, byte.class);
        map.put(Short.TYPE, short.class);
        map.put(Short.class, short.class);
        map.put(Integer.TYPE, int.class);
        map.put(Integer.class, int.class);
        map.put(Long.TYPE, long.class);
        map.put(Long.class, long.class);
        map.put(Float.TYPE, float.class);
        map.put(Float.class, float.class);
        map.put(Double.TYPE, double.class);
        map.put(Double.class, double.class);

        return map;
    }

    public static <T extends IDAble> ObservableList<ModelWrapper<T>> getListModel(List<T> list) {
        List<ModelWrapper<T>> innerList = new ArrayList<>();
        ObservableList<ModelWrapper<T>> observableList = FXCollections.observableList(innerList);

        innerList.addAll(list.stream().map(ModelWrapper::new).collect(Collectors.toList()));

        return observableList;
    }

    public static MappedGridPane createSingleStringGridPane(String instance) {
        MappedGridPane pane = new MappedGridPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setHgap(10);
        pane.setVgap(10);

        Node node = createTextField(instance);
        pane.add("actionableTextField", node,1,0);

        pane.setStyle("-fx-background-fill: black, white ; -fx-background-insets: 0, 1 ;");

        return pane;
    }

    public static <T> MappedGridPane createGridPane(Field[] fields, T instance) {
        MappedGridPane pane = new MappedGridPane();
        return refreshMappedGridPane(fields, instance, pane);
    }

    public static <T> MappedGridPane refreshMappedGridPane(Field[] fields, T instance, MappedGridPane pane) {
        pane.getChildren().clear();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setHgap(10);
        pane.setVgap(10);
        for(int row = 0; row < fields.length; row++) {
            Field field = fields[row];
            if(field.getAnnotation(MgmtIgnoreField.class) != null || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            add(row, pane, field, instance);
        }

        pane.setStyle("-fx-background-fill: black, white ; -fx-background-insets: 0, 1 ;");
        return pane;
    }

    private static void add(int row, MappedGridPane pane, Field field, Object instance) {
        pane.add(new Label(getLabel(field)),0,row);
        pane.add(field.getName(), getNodeOnType(field,instance),1,row);
    }

    private static String getLabel(Field field) {
        if(field.isAnnotationPresent(MgmtDisplayName.class)) {
            return field.getAnnotation(MgmtDisplayName.class).name();
        }

        String formatted = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
        return Joiner.on(' ').join(Splitter.on('_').trimResults().split(formatted));
    }

    private static String getDisplayFieldValue(Class clazz, Object instance) throws IllegalAccessException {
        return getDisplayFieldValue(clazz.getDeclaredFields(), clazz, instance);
    }

    private static String getDisplayFieldValue(Field[] fields, Class clazz, Object instance) throws IllegalAccessException {
        String field = getDisplayFieldValue(fields, instance);
        if (field != null) return field;

        return clazz.getName();
    }

    private static String getDisplayFieldValue(Field[] fields, Object instance) throws IllegalAccessException {
        for(Field field : fields) {
            if(field.isAnnotationPresent(MgmtDisplayField.class)) {
                field.setAccessible(true);
                return (String) field.get(instance);
            }
        }
        return null;
    }

    private static String getValueString(Field field, Object instance) {
        if(instance == null) {
            return "[null]";
        }
        Object fieldValue = getValueConverted(instance, field);
        return getValueString(fieldValue);
    }

    private static Object getValueConverted(Object fieldValue, Field field) {
        if(field.isAnnotationPresent(MgmtLong2DateConvert.class)) {
            return new Date(Long.valueOf(fieldValue.toString()));
        } else {
            return fieldValue;
        }
    }

    private static String getValueString(Object fieldValue) {
        if(fieldValue == null) {
            return "[null]";
        } else {
            Class<?> clazz = fieldValue.getClass();
            if(clazz.isAnnotationPresent(MgmtPojoModel.class)) {
                String field = null;
                try {
                    field = getDisplayFieldValue(clazz.getDeclaredFields(), fieldValue);
                } catch (IllegalAccessException e) {
                    getObjectToString(fieldValue);
                }
                if (field != null) {
                    return field;
                } else {
                    return getObjectToString(fieldValue);
                }
            } else {
                return getObjectToString(fieldValue);
            }
        }
    }

    private static String getObjectToString(Object fieldValue) {
        String string = fieldValue.toString();
        return string.length() > 55 ? string.substring(0, 52) + "..." : string;
    }

    private static Node getNodeOnType(Field field, Object instance) {
        Node node = getNode(field, instance);

        if(node == null) {
            return new Label("Unknown type: " + field.getName() + "(" + field.getType() + ")");
        } else {
            return node;
        }
    }

    private static Node getNode(Field field, Object instance) {
        Node node = null;
        if(field.getType() == Boolean.TYPE || field.getType() == boolean.class) {
            node = createCheckBox(field, instance);
        } else if(field.isAnnotationPresent(MgmtExternalId.class)) {
            node = createBoxOnExternalId(field, instance);
        } else if(field.isAnnotationPresent(MgmtFileOpener.class)) {
            node = createFileOpener(field, instance, field.getAnnotation(MgmtFileOpener.class).endpointHint());
        } else if(field.isAnnotationPresent(MgmtInternalList.class)) {
            node = createComboBoxFromFile(field, instance, field.getAnnotation(MgmtInternalList.class).file());
        } else if(PRIMITIVE_CONVERTER_MAP.containsKey(field.getType()) || field.getType() == String.class) {
            node = createTextField(field, instance);
        } else if(field.isAnnotationPresent(MgmtCoerceTypeToObject.class)) {
            node = createTextFieldFromCoercion(field, instance);
        } else if(field.getType().isEnum()) {
            node = createComboBoxFromEnum(field, instance);
        } else if(field.getType().isAnnotationPresent(MgmtPojoModel.class)) {
            node = createInternalModelBox(field, instance);
        } else if(field.isAnnotationPresent(MgmtIntegrationsMap.class)) {
            node = createInternalModelIntegrationsMap(field, instance);
        } else if(field.getGenericType() instanceof ParameterizedType) {
            if(((ParameterizedType)field.getGenericType()).getRawType() == List.class) {
                Class clazz = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                if(clazz != null && clazz.isAnnotationPresent(MgmtPojoModel.class)) {
                    node = createInternalModelList(field, instance, clazz);
                } else if (clazz != null && clazz == String.class) {
                    node = createStringList(field, instance);
                }
            }
        }

        if(node != null && !isEditable(field)) {
            node.setDisable(true);
        }

        return node;
    }

    private static ComboBox createComboBoxFromFile(Field field, Object instance, String fileName) {
        ObservableList<String> list = internalListMap.containsKey(fileName) ? internalListMap.get(fileName) : getObservableListFromFile(fileName);
        ComboBox<String> box = new ComboBox<>(list);
        initSetupComboBox(field, instance, box);
        return box;
    }

    public static ObservableList<String> getObservableListFromFile(String fileName) {
        List<String> list = new ArrayList<>();
        if(!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(ReflectUICreator.class.getResourceAsStream(fileName)))) {
            while(reader.ready()) {
                String token = reader.readLine();
                if(StringUtils.isNotBlank(token)) {
                    list.add(token);
                }
            }
            ObservableList<String> observableList = FXCollections.observableArrayList(list);
            internalListMap.put(fileName, observableList);
            return observableList;
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found",e);
        } catch (IOException e) {
            LOGGER.error("Could not read lines from file",e);
        }

        return FXCollections.emptyObservableList();
    }

    private static HBox createFileOpener(Field field, Object instance, String endpoint) {
        Button button = new Button("Change...");
        if(instance instanceof IDAble && ((IDAble)instance).getId() == null) {
            button.setDisable(true);
        }
        button.setOnMouseClicked(event -> {
            File chosen = FileChooserPopup.showImagePopup("Select an image file...");
            if(chosen != null && chosen.getName().endsWith("png")) {
                chosen = null;
            }
            byte[] bytes = new byte[0];
            try {
                if(chosen != null) {
                    bytes = Files.readAllBytes(chosen.toPath());
                }
            } catch (IOException e) {
                LOGGER.error("Cannot read file", e);
            }
            Map<String,Object> queryMap = new HashMap<>();
            Object fieldValue = reflectGetter(field, instance);
            if(fieldValue != null) {
                queryMap.put(field.getName(), fieldValue);
            }

            if(bytes.length > 0) {
                //noinspection ConstantConditions
                WebService.getWebService().post(Endpoints.MANAGEMENT + "/" + endpoint + "/" + ((IDAble) instance).getId(), bytes, IdPojo.class, queryMap, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM);
            }
        });

        return new HBox(new Label(reflectGetter(field, instance)), button);
    }

    @SuppressWarnings("unchecked")
    private static HBox createInternalModelBox(Field field, Object instance) {
        HBox hBox = new HBox();
        Object fieldInstance = reflectGetter(field, instance);
        Label label = new Label(getValueString(field, fieldInstance));
        hBox.getChildren().add(label);
        Button button = new Button(fieldInstance == null ? "Create" : "Edit");
        button.setOnMouseClicked(x -> {
            Object currentInstanceValue = reflectGetter(field, instance);
            GenericFormPopup popup = new GenericFormPopup(field.getType(), currentInstanceValue, currentInstanceValue == null);
            popup.setOnClose(fieldValue -> {
                setIdOnInternalIDAble(instance, fieldValue);
                reflectSetter(field,instance,fieldValue);
                Object newInstanceValue = reflectGetter(field, instance);
                label.setText(getValueString(newInstanceValue));
                button.setText(newInstanceValue == null ? "Create" : "Edit");
            });
            popup.show();
        });
        hBox.getChildren().add(button);
        return hBox;
    }

    @SuppressWarnings("unchecked")
    private static Node createStringList(Field field, Object instance) {
        field.setAccessible(true);

        VBox outerBox = new VBox();
        VBox buttonBox = new VBox();
        outerBox.getChildren().add(buttonBox);
        List list;

        try {
            list = (List)field.get(instance);
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not get list", e);
            return new Label("Cannot edit");
        }
        for (int i = 0; i < list.size(); i++) {
            String item = (String)list.get(i);
            HBox stringDeleteButton = createStringDeleteButton(item);
            buttonBox.getChildren().add(stringDeleteButton);

            (stringDeleteButton.getChildren().get(0)).setOnMouseClicked(e -> {
                list.remove(item);
                buttonBox.getChildren().remove(stringDeleteButton);
            });
        }

        Button button = new Button("Add");
        button.setOnMouseClicked(e -> GenericFormPopup.showStringPopup(newInstance -> {
            list.add(newInstance);
            HBox stringDeleteButton = createStringDeleteButton(newInstance);
            buttonBox.getChildren().add(stringDeleteButton);

            (stringDeleteButton.getChildren().get(0)).setOnMouseClicked(e2 -> {
                list.remove(newInstance);
                buttonBox.getChildren().remove(stringDeleteButton);
            });
        }));
        outerBox.getChildren().add(new HBox(10, button));

        return outerBox;
    }

    @SuppressWarnings("unchecked")
    private static Node createInternalModelList(Field field, Object instance, Class clazz) {
        field.setAccessible(true);
        Field[] fields = clazz.getDeclaredFields();

        VBox outerBox = new VBox();
        VBox buttonBox = new VBox();
        outerBox.getChildren().add(buttonBox);
        List list;
        try {
            list = (List)field.get(instance);
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not get list", e);
            return new Label("Cannot edit");
        }
        for (Object object : list) {
            buttonBox.getChildren().add(createEditButtons(clazz, fields, object, list, buttonBox));
            if(object instanceof Floor) {
                Button moveUp = new Button("Move Up");
                Button moveDown = new Button("Move Down");
                buttonBox.getChildren().add(new HBox(moveUp, moveDown));
                moveUp.setOnMouseClicked(e -> {
                    moveObjectUp(list, object);
                });
                moveDown.setOnMouseClicked(e -> {
                    moveObjectDown(list, object);
                });
            }
        }
        Button button = new Button("Add");
        button.setOnMouseClicked(e -> GenericFormPopup.show(clazz, null, true, newInstance -> {
            if(newInstance instanceof IDAble && instance instanceof IDAble) {
                ((IDAble)newInstance).setId(IDAble.generateId(((IDAble)instance).getId()));

                //special logic for Floor
                if(newInstance instanceof Floor) {
                    Floor floor = (Floor)newInstance;
                    Layout layout = new Layout(floor);
                    layout.setUpdated(System.currentTimeMillis());
                    layout.setName("Default Layout");
                    floor.getLayouts().add(layout);
                    floor.setActiveLayout(layout.getId());
                }
            }
            list.add(newInstance);
            buttonBox.getChildren().add(createEditButtons(clazz, fields, newInstance, list, buttonBox));
        }));


        outerBox.getChildren().add(new HBox(10, button));


        return outerBox;
    }

    private static void moveObjectDown(List list, Object object) {
        if(!(object instanceof Floor)) {
            return;
        }
        Floor idAble = (Floor) object;
        int found = -1;
        found = findFloor(list, idAble, found);
        if(found < 0 || found > (list.size()-1)) {
            return;
        }

        Object o1 = list.get(found);
        Object o2 = list.get(found+1);
        list.set(found, o2);
        list.set(found+1, o1);
    }

    private static int findFloor(List list, Floor idAble, int found) {
        for (int i = 0; i < list.size(); i++) {
            if (idAble.getId() != null && idAble.getId().equals(((IDAble) list.get(i)).getId())) {
                found = i;
                break;
            }
        }
        return found;
    }

    private static void moveObjectUp(List list, Object object) {
        if(!(object instanceof Floor)) {
            return;
        }
        Floor idAble = (Floor) object;
        int found = -1;
        found = findFloor(list, idAble, found);
        if(found <= 0) {
            return;
        }

        Object o1 = list.get(found);
        Object o2 = list.get(found-1);
        list.set(found, o2);
        list.set(found-1, o1);
    }

    @SuppressWarnings("unchecked")
    private static HBox createEditButtons(Class clazz, Field[] fields, Object object, List list, Pane parent) {
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");
        HBox hBox = new HBox(10, getLabel(object, clazz, fields), edit, delete);

        if(clazz == RestaurantDefault.class) {
            Object value = ((RestaurantDefault)object).getValue();
            String valueString = getValueString(value);
            hBox.getChildren().add(new Label(valueString));
        }

        edit.setOnMouseClicked(e -> GenericFormPopup.show(clazz, object, false, editedInstance -> {
            //nothing to do
        }));

        delete.setOnMouseClicked(e -> {
            list.remove(object);
            parent.getChildren().remove(hBox);
        });

        return hBox;
    }

    private static HBox createStringDeleteButton(String string) {
        Button delete = new Button("Delete");
        HBox hBox = new HBox(10, new Label(string), delete);

        return hBox;
    }

    @SuppressWarnings("unchecked")
    private static Node createInternalModelIntegrationsMap(Field field, Object instance) {
        Map<ExternalIntegration, KVData> map;
        try {
            map = (Map<ExternalIntegration, KVData>) field.get(instance);
        } catch (IllegalAccessException e) {
            LOGGER.error("Cannot read integrations map", e);
            return new Label("Cannot create elements");
        }
        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox(10);
        borderPane.setCenter(vBox);
        for(Map.Entry<ExternalIntegration, KVData> entry : map.entrySet()) {
            vBox.getChildren().add(getIntegrationsBox(map, entry.getKey(), entry.getValue(), vBox));
        }
        vBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        Button addButton = new Button("Add");
        addButton.setOnMouseClicked(e -> {
            if(!map.containsKey(ExternalIntegration.NONE)) {
                map.put(ExternalIntegration.NONE, new KVData());
                vBox.getChildren().add(getIntegrationsBox(map, ExternalIntegration.NONE, map.get(ExternalIntegration.NONE), vBox));
            }
        });
        borderPane.setBottom(addButton);
        return borderPane;
    }

    @SuppressWarnings("unchecked")
    private static HBox getIntegrationsBox(Map<ExternalIntegration, KVData> map, ExternalIntegration externalIntegration, KVData kvData, VBox container) {
        ComboBox<ExternalIntegration> comboBox = new ComboBox<>(FXCollections.observableArrayList(ExternalIntegration.values()));
        comboBox.getSelectionModel().select(externalIntegration);
        Button deleteButton = new Button("Delete");
        ReflectionView kvDataView = PojoViewBuilder.newInstance().withClass(KVData.class).withWebService(WebService.getWebService()).withInstance(kvData).asView();
        HBox hBox = new HBox(comboBox, kvDataView, deleteButton);
        kvDataView.getAffirmativeButton().setOnMouseClicked(e -> {
            KVData data = map.remove(externalIntegration);
            map.put(comboBox.getValue(), data);
        });
        deleteButton.setOnMouseClicked(e -> {
            map.remove(externalIntegration);
            container.getChildren().remove(hBox);
        });
        kvDataView.getClearButton().setDisable(kvData.getData().size() == 0);
        kvDataView.getClearButton().setOnMouseClicked(e -> {
            kvData.getData().clear();
            kvDataView.getClearButton().setDisable(kvData.getData().size() == 0);
        });

        return hBox;
    }

    private static Label getLabel(Object instance, Class actualType, Field[] fields) {
        String labelString = actualType.getName();
        try {
            labelString = getDisplayFieldValue(fields, actualType, instance);
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not get class display field", e);
        }
        return new Label(labelString);
    }

    private static void setIdOnInternalIDAble(Object instance, Object fieldValue) {
        if(fieldValue != null  && fieldValue instanceof IDAble) {
            IDAble idAble = (IDAble)fieldValue;
            if (idAble.getId() == null && instance instanceof IDAble) {
                idAble.setId(IDAble.generateId(((IDAble) instance).getId()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static ComboBox createComboBoxFromEnum(Field field, Object instance) {
        ComboBox box = new ComboBox();
        Object[] enums = field.getType().getEnumConstants();
        box.setItems(FXCollections.observableArrayList(enums));
        initSetupComboBox(field, instance, box);
        return box;
    }

    @SuppressWarnings("unchecked")
    private static void initSetupComboBox(Field field, Object instance, ComboBox box) {
        box.setOnAction(x -> reflectSetter(field, instance, box.getSelectionModel().getSelectedItem()));
        Object currentState = reflectGetter(field, instance);
        if(currentState != null) {
            box.getSelectionModel().select(currentState);
        }
    }

    private static HBox createBoxOnExternalId(Field field, Object instance) {
        MgmtExternalId mgmtExternalId = field.getAnnotation(MgmtExternalId.class);
        if(mgmtExternalId.listView()) {
            return createBoxOnExternalIdAsList(field, instance, mgmtExternalId);
        } else {
            return createBoxOnExternalIdAsCombo(field, instance, mgmtExternalId);
        }
    }

    private static HBox createBoxOnExternalIdAsCombo(Field field, Object instance, MgmtExternalId mgmtExternalId) {
        WebService webService = WebService.getWebService();
        ComboBox<ModelWrapper> box = new ComboBox<>();
        box.setOnAction(e -> {
            ModelWrapper wrapper = box.getSelectionModel().getSelectedItem();
            if(wrapper == null) {
                return;
            }

            reflectSetter(field, instance, wrapper.getId());
        });
        box.setItems(FXCollections.observableArrayList());
        String endpoint = StringUtils.isNotBlank(mgmtExternalId.endpoint()) ? mgmtExternalId.endpoint() : instance.getClass().getCanonicalName();
        if(mgmtExternalId.restrictOnParentId() && instance != null && instance instanceof IDAble) {
            endpoint += "/" + getParentId((IDAble) instance, mgmtExternalId);
        }
        final String finalEndpoint = endpoint;
        refreshList(webService, mgmtExternalId, box, finalEndpoint);
        Object currentState = reflectGetter(field, instance);
        if(currentState != null) {
            ModelWrapper current = box.getItems().stream().filter(m -> m.getUnderlying().getId().equals(currentState)).findFirst().orElse(null);
            box.getSelectionModel().select(current);
        }

        Button addButton = new Button("Add");
        addButton.setOnMouseClicked(e -> GenericFormPopup.show(mgmtExternalId.externalClass(), null, true, fieldValue -> {
            Field idSetter = findParentIdField(mgmtExternalId.externalClass());
            if(idSetter != null && instance instanceof IDAble) {
                idSetter.setAccessible(true);
                reflectSetter(idSetter,fieldValue,((IDAble)instance).getId());
            }
            setIdOnInternalIDAble(instance, fieldValue);
            webService.post(Endpoints.MANAGEMENT + "/" + mgmtExternalId.externalClass().getCanonicalName(), fieldValue, Object.class);
            refreshList(webService, mgmtExternalId, box, finalEndpoint);
            if(box.getItems().size() > 0) {
                box.getSelectionModel().select(box.getItems().size()-1);
            }
        }));
        Button editButton = new Button("Edit");
        editButton.setOnMouseClicked(e -> GenericFormPopup.show(mgmtExternalId.externalClass(), null, true, fieldValue -> {
            webService.put(Endpoints.MANAGEMENT + "/" + mgmtExternalId.externalClass().getCanonicalName() + "/" + fieldValue.getId(), fieldValue);
        }));

        VBox vBox = new VBox(addButton, editButton);

        return new HBox(box, vBox);
    }

    private static void refreshList(WebService webService, MgmtExternalId mgmtExternalId, ComboBox<ModelWrapper> box, String endpoint) {
        List<? extends IDAble> list = webService.getAsList(Endpoints.MANAGEMENT + "/" + endpoint, mgmtExternalId.externalClass());
        box.getItems().clear();
        box.getItems().addAll(list.stream().map(ModelWrapper::new).collect(Collectors.toList()));
    }

    private static String getParentId(IDAble instance, MgmtExternalId mgmtExternalId) {
        String current = instance.getId();
        for(int i = 0; i < mgmtExternalId.traverseToParent(); i++) {
            if(current == null) {
                break;
            }
            current = IDAble.extractParentId(current);
        }
        return current;
    }

    @SuppressWarnings({"unchecked", "EqualsBetweenInconvertibleTypes"})
    private static HBox createBoxOnExternalIdAsList(Field field, Object instance, MgmtExternalId mgmtExternalId) {
        ListViewBuilder<ModelWrapper> viewBuilder = ListViewBuilder.newInstance().withClass(mgmtExternalId.externalClass()).withWebService(WebService.getWebService());
        if(mgmtExternalId.restrictOnRestaurantId() && instance instanceof Restaurant) {
            viewBuilder.withRestaurantId(((Restaurant)instance).getId());
        }
        ListView listView1 = viewBuilder.asList();
        listView1.setPrefHeight(80);
        ObservableList<ModelWrapper> existingListInEntity = FXCollections.observableArrayList();
        ListView listView2 = new ListView(existingListInEntity);
        listView2.setPrefHeight(80);

        //fill current list with current values
        final boolean isListOfIDs = isListOfIDs(field, instance);
        List current = reflectGetter(field, instance);
        List<ModelWrapper> sourceListOfWrappers = listView1.getItems();
        if(isListOfIDs) {
            existingListInEntity.addAll(sourceListOfWrappers.stream().filter(wrapper -> current.contains(wrapper.getId())).collect(Collectors.toList()));
            sourceListOfWrappers.removeIf(w -> current.contains(w.getId()));
        } else {
            for(ModelWrapper wrapper : sourceListOfWrappers) {
                if(current.contains(wrapper.getUnderlying())) {
                    continue;
                }
                current.stream().filter(inCurrentList -> ((IDAble) inCurrentList).getId().equals(wrapper.getId())).forEach(inCurrentList -> {
                    existingListInEntity.add(wrapper);
                });
            }
        }

        Button addExisting = new Button(">>");
        addExisting.setOnMouseClicked(e -> {
            ModelWrapper wrapper = (ModelWrapper)listView1.getSelectionModel().getSelectedItem();
            if(wrapper == null) return;

            if(isListOfIDs) {
                current.add(wrapper.getId());
            } else {
                current.add(wrapper.getUnderlying());
            }
            existingListInEntity.add(wrapper);
            sourceListOfWrappers.remove(wrapper);
        });

        Button removeExisting = new Button("<<");
        removeExisting.setOnMouseClicked(e -> {
            Object object = listView2.getSelectionModel().getSelectedItem();
            if(object == null) return;

            ModelWrapper wrapper = (ModelWrapper) object;
            if(isListOfIDs) {
                current.remove(wrapper.getId());
            } else {
                current.removeIf(w -> ((IDAble)w).getId().equals(wrapper.getId()));
            }

            existingListInEntity.removeIf(m -> m.getId().equals(wrapper.getId()));
            sourceListOfWrappers.add(wrapper);
        });

        return new HBox(new ScrollPane(listView1), new VBox(addExisting, removeExisting), new ScrollPane(listView2));
    }

    private static boolean isListOfIDs(Field field, Object instance) {
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
        if(listClass == String.class) {
            return true;
        } else {
            return false;
        }
    }

    private static Field findParentIdField(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            if(field.isAnnotationPresent(MgmtSetTopLevelId.class)) {
                return field;
            }
        }
        return null;
    }

    private static TextInputControl createTextField(Field field, Object instance) {
        TextInputControl textField;
        if(field.isAnnotationPresent(MgmtPassword.class)) {
            textField = new PasswordField();
        } else {
            textField = new TextField();
        }
        if(field.getName().equals("id")) {
            textField.setEditable(false);
            //textField.setDisable(true);
        } else {
            textField.setPromptText(field.getType().getName());
        }
        textField.setOnKeyReleased(x -> {
            if(PRIMITIVE_CONVERTER_MAP.containsKey(field.getType())) {
                reflectSetter(field, instance, PRIMITIVE_CONVERTER_MAP.get(field.getType()).fromString(textField.getText() == null ? "" : textField.getText()));
            } else {
                reflectSetter(field, instance, textField.getText());
            }
        });
        Object value = reflectGetter(field, instance);
        if(value != null) {
            textField.setText(value.toString());
        }
        return textField;
    }

    private static TextInputControl createTextField(String instance) {
        TextInputControl textField = new TextField();
        textField.setPromptText("String");

        if(instance != null) {
            textField.setText(instance);
        }
        return textField;
    }

    private static Node createTextFieldFromCoercion(Field field, Object instance) {
        TextField textField = new TextField();
        ComboBox<CoercionType> comboBox = new ComboBox<>(FXCollections.observableArrayList(CoercionType.values()));
        comboBox.getSelectionModel().select(CoercionType.NONE);
        Object value = reflectGetter(field, instance);
        if(value != null) {
            textField.setText(value.toString());

            if(value instanceof String) {
                comboBox.getSelectionModel().select(CoercionType.STRING);
            } else if(value instanceof Double) {
                comboBox.getSelectionModel().select(CoercionType.DOUBLE);
            } else if(value instanceof Integer) {
                comboBox.getSelectionModel().select(CoercionType.INTEGER);
            } else if(value instanceof Boolean) {
                comboBox.getSelectionModel().select(CoercionType.BOOLEAN);
            }
        }
        textField.setOnKeyReleased(x -> setValueFromCoercion(field, instance, textField, comboBox));
        comboBox.setOnAction(e -> setValueFromCoercion(field, instance, textField, comboBox));

        return new HBox(10, textField, comboBox);
    }

    private static void setValueFromCoercion(Field field, Object instance, TextField textField, ComboBox<CoercionType> comboBox) {
        if(textField.getText() == null) {
            return;
        }

        if(comboBox.getSelectionModel().getSelectedItem() == CoercionType.STRING) {
            reflectSetter(field, instance, textField.getText());
        } else if(comboBox.getSelectionModel().getSelectedItem() == CoercionType.DOUBLE) {
            try {
                reflectSetter(field, instance, Double.valueOf(textField.getText()));
            } catch (NumberFormatException ex){}
        } else if(comboBox.getSelectionModel().getSelectedItem() == CoercionType.INTEGER) {
            try {
                reflectSetter(field, instance, Integer.valueOf(textField.getText()));
            } catch (NumberFormatException ex){}
        } else if(comboBox.getSelectionModel().getSelectedItem() == CoercionType.BOOLEAN) {
            reflectSetter(field, instance, Boolean.valueOf(textField.getText()));
        }
    }

    private static CheckBox createCheckBox(Field field, Object instance) {
        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(x -> reflectSetter(field, instance, checkBox.isSelected()));
        Boolean currentState = reflectGetter(field, instance);
        if (currentState != null) {
            checkBox.setSelected(currentState);
        }
        return checkBox;
    }

    private static boolean isEditable(Field field) {
        return !field.isAnnotationPresent(MgmtEditableField.class) || field.getAnnotation(MgmtEditableField.class).editable();

    }

    private static void reflectSetter(Field field, Object instance, Object newValue) {
        if(useGettersAndSetters(instance)) {
            reflectSetterByMethod(field, instance, newValue);
        } else {
            reflectSetterByField(field, instance, newValue);
        }
    }

    private static void reflectSetterByMethod(Field field, Object instance, Object newValue) {
        String methodName = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1,field.getName().length());
        Method method = null;
        try {
            method = getMethod(instance, methodName, newValue.getClass());
        } catch (NoSuchMethodException e) {
            if(PRIMITIVE_MAP.containsKey(newValue.getClass())) {
                try {
                    method = getMethod(instance, methodName, PRIMITIVE_MAP.get(newValue.getClass()));
                } catch (NoSuchMethodException e1) {
                    LOGGER.error("Cannot find method", e1);
                }
            }
        }
        if(method == null) {
            throw new IllegalArgumentException("Cannot find setter method for field " + field);
        }
        try {
            method.invoke(instance, newValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Cannot invoke method", e);
        }
    }

    private static Method getMethod(Object instance, String methodName, Class<?>... args) throws NoSuchMethodException {
        return instance.getClass().getMethod(methodName, args);
    }

    private static void reflectSetterByField(Field field, Object instance, Object newValue) {
        try {
            field.set(instance, newValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean useGettersAndSetters(Object instance) {
        return instance.getClass().isAnnotationPresent(MgmtPojoModel.class)
                && instance.getClass().getAnnotation(MgmtPojoModel.class).useAccessMethods();
    }


    private static <S> S reflectGetter(Field field, Object instance) {
        if(instance == null) {
            return null;
        }

        if(useGettersAndSetters(instance)) {
            return reflectGetterByMethod(field, instance);
        } else {
            return reflectGetterByField(field, instance);
        }
    }

    @SuppressWarnings("unchecked")
    private static <S> S reflectGetterByField(Field field, Object instance) {
        try {
            return (S) field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <S> S reflectGetterByMethod(Field field, Object instance) {
        String methodName = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1,field.getName().length());
        try {
            Method method = instance.getClass().getMethod(methodName);
            return (S)method.invoke(instance);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Cannot invoke method", e);
        }
        return null;
    }
}
