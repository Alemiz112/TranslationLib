package eu.mizerak.alemiz.translationlib.service.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value={METHOD, TYPE})
@Retention(value=RUNTIME)
public @interface Roles {

    /**
     * Specify the permitted roles.
     */
    AccessRole[] value() default {};
}
