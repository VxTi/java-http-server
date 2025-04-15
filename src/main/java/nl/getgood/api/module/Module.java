package nl.getgood.api.module;

import nl.getgood.api.Database;
import nl.getgood.api.Logger;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.Response;
import nl.getgood.api.server.RegisteredApiRoute;
import nl.getgood.api.server.Route;
import nl.getgood.api.server.Server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 03/08/2024 at 15:33
 * by Luca Warmenhoven.
 */
public abstract class Module
{

    public Server server;
    public Database database = null;

    /**
     * Initialize the module.
     * This method will be called when the module is loaded by the api server.
     *
     * @param server The api server that the module is being loaded by.
     */
    public void initialize( Server server, Database database ) {
        this.server = server;
        this.database = database;
    }

    /**
     * Close the module.
     * This method will be called when the module is being closed by the api server.
     */
    public void close() {}

    /**
     * Get all api handlers in this module.
     * This method will scan all methods in this module and return a map containing all api handlers.
     *
     * @return A map containing all api handlers in this module.
     */
    public final List<RegisteredApiRoute> getApiHandlers()
    {

        Method[] methods = this.getClass().getDeclaredMethods();
        List<RegisteredApiRoute> handlers = new ArrayList<>();
        Route route;

        for ( Method method : methods )
        {
            Class<?>[] parameterTypes = method.getParameterTypes();

            // Check if the method is a valid api route handler
            // (has the ApiRoute annotation, has 2 parameters, and the parameters are of type Request and Response).
            if ( method.isAnnotationPresent( Route.class )
                    && parameterTypes.length == 2
                    && parameterTypes[0].equals( Request.class )
                    && parameterTypes[1].equals( Response.class ) )
            {
                route = method.getAnnotation( Route.class );

                handlers.add( new RegisteredApiRoute( route.route(), ( ( request, response ) ->
                {
                    try
                    {
                        method.invoke( this, request, response );
                    }
                    catch ( Exception e )
                    {
                        Logger.getLogger().error( "Failed to invoke method " + method.getName() + " in module " + this.getClass().getName());
                        Logger.getLogger().errorStack( e );
                    }
                } ), route.method() ) );
            }
        }
        return handlers;
    }

}
