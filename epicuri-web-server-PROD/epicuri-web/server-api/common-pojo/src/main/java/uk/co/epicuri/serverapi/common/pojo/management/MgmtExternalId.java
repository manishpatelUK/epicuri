package uk.co.epicuri.serverapi.common.pojo.management;

import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes the field is an ID for some other object
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MgmtExternalId {
    /**
     * Type of other object
     * @return the class of object
     */
    Class<? extends IDAble> externalClass();

    /**
     * What the type endpoint is, e.g. Cuisines or Printers. See ManagementController
     * @return
     */
    String endpoint() default "";

    /**
     * Tells the back end to restrict on parent id, e.g. restaurant id. See how Printers works
     * @return
     */
    boolean restrictOnParentId();

    /**
     * The default UI is a combobox. This sets it to be a list instead.
     * @return
     */
    boolean listView() default false;

    /**
     * Traverses up the child/parent tree of IDAble objects x times to determine the correct id to use on the endpoint
     * 0 means use the immediate container's id
     * @return
     */
    int traverseToParent() default 0;

    /**
     * Further restrict full list on rest id that is on the object: will check field "restaurantId"
     * @return
     */
    boolean restrictOnRestaurantId() default false;
}
