package nl.getgood.api.server;

import nl.getgood.api.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Api Route annotation.
 * This annotation is used to define the route of an api endpoint.
 * Created on 03/08/2024 at 15:30
 * by Luca Warmenhoven.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route
{
    String route();
    RequestMethod method();
}
