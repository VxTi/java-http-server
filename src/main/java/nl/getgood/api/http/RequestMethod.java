package nl.getgood.api.http;

/**
 * Created on 03/08/2024 at 16:24
 * by Luca Warmenhoven.
 */
public enum RequestMethod {

    OPTIONS,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    TRACE,
    CONNECT;

    public static RequestMethod parse(String requestHeaderParam) {
        var requestMethod = RequestMethod.GET;
        for (var method : RequestMethod.values()) {
            if (method.name().equalsIgnoreCase(requestHeaderParam)) {
                requestMethod = method;
                break;
            }
        }
        return requestMethod;
    }
}
