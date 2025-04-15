package nl.getgood.api.http;

/**
 * Created on 04/08/2024 at 19:18
 * by Luca Warmenhoven.
 */
public interface IRequestHandler
{
    /**
     * Handles a request.
     * This method is called when a request is received by the server.
     * The handler can then handle the request and return a response.
     *
     * @param request The request that was received by the server.
     * @param response The response that will be sent to the client.
     */
    void handleRequest(Request request, Response response);
}
