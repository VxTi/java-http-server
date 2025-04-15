package nl.getgood.api.middleware;

import nl.getgood.api.http.Request;
import nl.getgood.api.http.Response;

/**
 * Middleware interface
 * This interface is used to create middleware for the server,
 * which can be used to handle requests before they are passed to the API.
 * <br /> <br />
 * Created on 04/08/2024 at 18:38
 * by Luca Warmenhoven.
 */
public interface Middleware
{
    /**
     * Handles a request.
     * This method is called when a request is received by the server.
     * The middleware can then handle the request and return a boolean value
     * to indicate whether the request should be forwarded to the API or not.
     *
     * @param request The request that was received by the server.
     * @param response The response that will be sent to the client.
     * @return True if the request can be forwarded to the API,
     * false if the request should be stopped.
     */
    boolean handleRequest(Request request, Response response);
}
