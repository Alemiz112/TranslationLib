package eu.mizerak.alemiz.translationlib.service.utils.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface JsonFieldAdapter {

    String value();

    Mode mode() default Mode.ALL;

    enum Mode {
        READ,
        WRITE,
        ALL;
    }
}
