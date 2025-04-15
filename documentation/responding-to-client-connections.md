# Responding to Client Connections

---

If one is using the `Server` class to create a server, one can respond to client connections by registering
handlers using the classes defined in the `nl.getgood.api` package. 
Using the `Server` class, one can create a new connection handler by calling the `get`, `post`, `put`, or `delete` methods.
These methods accept a route and a handler as arguments. The route is a string that defines the path to the resource,
and the handler is a function that is called when a client makes a request to the specified route.

The example below demonstrates how to create a simple server that listens on the local IP address on port 80.

```java

import nl.getgood.api.server.Server;

public class MyServer {

    public static void main( String[] args )
    {
        Server server = Server.create(80);
        
        server.get("/", (request, response) -> {
            // Do something with the request
        });
    }
}
```

In this example, we create a handler that listens for GET requests to the root path ("/"). 
When a client makes a GET request to the root path, the handler function is called.

The handler function takes two arguments: a `Request` object and a `Response` object.

The `Request` class contains a variety of fields and methods that provide information about the made HTTP request.
These are as followed:

> - `requestMethod` (`RequestMethod`): The HTTP method used in the request (GET, POST, PUT, DELETE, etc.)'
> - `path` (`String`): The path of the request
> - `headers` (`Map<String, String>`): A map of the request headers
> - `body` (`String`): The body of the request
> - `queryParameters` (`Map<String, String>`): A map of the query parameters in the request
> - `cookies` (`Map<String, String>`): A map of the cookies in the request
> - `bodyAsJson()` (`JsonObject`): The body of the request as a JSON object

The `Response` class contains a variety of methods that allow one to send a response to the client.
These are as followed:

> - `status(StatusCode status)`: Sets the status code of the response
> - `header(String key, String value)`: Adds a header to the response
> - `body(String body)`: Sets the body of the response
> - `body(JsonObject body)`: Sets the body of the response as a JSON object
> - `cookie(String key, String value)`: Adds a cookie to the response
> - `redirect(String location)`: Redirects the client to the specified location
> - `protocol(Protocol protocol)`: Sets the protocol of the response