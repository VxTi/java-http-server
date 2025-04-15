package nl.getgood.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 04/08/2024 at 15:38
 * by Luca Warmenhoven.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget
{
    EventPriority priority() default EventPriority.NORMAL;

    boolean cancellable() default false;

    String eventName();

}
