# Registering API Handlers

---

One can register API handlers using the classes defined in the `nl.getgood.api` package.
Our API server is defined by a group of modules, which can register their own API handlers.

In the example below, we create a simple API server using a custom module that handles
a predefined route.

```java
package com.example;

import nl.getgood.api.server.Route;
import nl.getgood.api.server.Server;
import nl.getgood.api.http.StatusCode;
import nl.getgood.api.module.Module;
import nl.getgood.api.module.ModuleInitializationFailedException;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;

class CustomerApiServer
{

    public static void main( String[] args )
    {
        Server server = new Server();
        server.useModule( new CustomerModule() );
        server.startListening();
    }

    public class CustomerModule extends Module
    {
        public CustomerModule()
        {
            registerHandler( new CustomerHandler() );
        }

        @Override
        public void initialize( Server apiServer ) throws ModuleInitializationFailedException
        {
            // Initialize the module
        }

        @Override
        public void close()
        {
            // Clean up resources
        }

        @Route(route = "/v1/customers", method = RequestMethod.GET)
        public void handleGetCustomers( Request request, Response response )
        {
            // Handle the GET request
            response.status( StatusCode.OK );
            response.body( "Hello, World!" );
        }
    }

}
```

The example shown above will create an API server that listens to the local IP address on port 80.
The server will respond to a GET request to the `/v1/customers` route with a `200 OK` status code and the body `Hello, World!`.