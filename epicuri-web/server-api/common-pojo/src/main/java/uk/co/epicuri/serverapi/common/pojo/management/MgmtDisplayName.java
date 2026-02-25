package uk.co.epicuri.serverapi.common.pojo.management;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tag a field with this to create a label next to the UI element of this name value
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MgmtDisplayName {
    String name();
}
