package uk.co.epicuri.serverapi.common.pojo.management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field will be populated by the value of a file, e.g. image
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MgmtFileOpener {
    /**
     * Which endpoint to use to upload the file
     * @return
     */
    String endpointHint();
}
