package dev.kitteh.factions.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// A copy to make the processor happy
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WipeOnReload {
}
