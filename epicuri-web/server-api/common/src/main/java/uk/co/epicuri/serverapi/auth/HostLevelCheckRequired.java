package uk.co.epicuri.serverapi.auth;

import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by manish
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HostLevelCheckRequired {
    StaffRole role() default StaffRole.UNKNOWN;
}
