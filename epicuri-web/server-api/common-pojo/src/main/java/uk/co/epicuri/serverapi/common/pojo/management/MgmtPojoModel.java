package uk.co.epicuri.serverapi.common.pojo.management;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tag a type that is not a "top level" entity, e.g. Address.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MgmtPojoModel {
    /**
     * Use getters and setters methods instead of direct field access
     * @return
     */
    boolean useAccessMethods() default false;

    /**
     * Use this class when creating new items of the model. The template class is expected to be a proper managed type, like Default
     * @return
     */
    Class<?> onNewTemplate() default NoTemplate.class;
}
