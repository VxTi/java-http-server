package nl.getgood.api.server;

import nl.getgood.api.http.IRequestHandler;
import nl.getgood.api.http.RequestMethod;

/**
 * Represents an API route entry.
 * This class is used to store information about an API route.
 * It is used to store the route, the module that the route is in, the request method of the route,
 * and the method that the route is handled by.
 * <br /> <br />
 * Created on 03/08/2024 at 17:25
 * by Luca Warmenhoven.
 */
public class RegisteredApiRoute
{

    private final String route;
    private final RequestMethod requestMethod;
    private final IRequestHandler requestHandler;

    /**
     * Creates a new API route entry instance.
     *
     * @param route          The route of the API endpoint.
     *                       This is the route that the endpoint will be hosted on.
     *                       For example: "/api/v1/test".
     * @param requestHandler The request handler that will be called when the route is requested.
     * @param requestMethod  The request method of the route.
     *                       This is the request method that the route will be hosted on.
     *                       For example: RequestMethod.GET or RequestMethod.POST.
     */
    public RegisteredApiRoute( String route, IRequestHandler requestHandler, RequestMethod requestMethod )
    {
        this.route = route;
        this.requestHandler = requestHandler;
        this.requestMethod = requestMethod;
    }

    /**
     * Gets the route of the API endpoint.
     *
     * @return The route of the API endpoint.
     */
    public String getRoute()
    {
        return route;
    }

    /**
     * Gets the request method of the route.
     *
     * @return The request method of the route.
     */
    public RequestMethod getRequestMethod()
    {
        return requestMethod;
    }

    /**
     * Gets the request handler that will be called when the route is requested.
     *
     * @return The request handler that will be called when the route is requested.
     */
    public IRequestHandler getRequestHandler()
    {
        return requestHandler;
    }
}
