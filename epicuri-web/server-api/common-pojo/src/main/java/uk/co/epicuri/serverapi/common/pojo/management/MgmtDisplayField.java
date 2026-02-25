package uk.co.epicuri.serverapi.common.pojo.management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag a field with this for that field to be used (toString) as the label inside list/combo box.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MgmtDisplayField {
}
