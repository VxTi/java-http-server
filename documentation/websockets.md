# Using WebSocket connections

---

With the getgood Server, one can register a websocket handler by extending the `IWebSocketHandler` class and overriding the `onMessage` method. The `onMessage` method is called whenever a message is received from a client.
Since the `IWebSocketHandler` is an interface with default methods, you can define the interface using
a lambda expression. An example of a simple websocket handler is shown below:

```java

import nl.getgood.api.http.websocket.WebSocketResponder;
import nl.getgood.api.server.Server;

public class MyServer
{

    public static void main( String[] args )
    {
        Server server = Server.create( 80 );
        server.websocket( "/", ( message, responder ) ->
        {
            System.out.println( "Received message: " + message );
            responder.send( "Hello, World!" );
        } );
        server.startListening();
    }
}
```

Or using the `IWebSocketHandler` class:

```java


import nl.getgood.api.http.websocket.IWebSocketHandler;
import nl.getgood.api.http.websocket.IWebSocketMessageResponder;
import nl.getgood.api.server.Server;

public class MyServer
{

    public static void main( String[] args )
    {
        // Create a server that listens on port 80
        Server server = Server.create( 80 );

        // Serve the websocket connection at "/"
        server.websocket( "/", ( message, responder ) ->
        {
            System.out.println( "Received message: " + message );

            // Reply to the client by calling the 'respond' method.
            responder.send( "Hello, World!" );
        } );
        // Start the server
        server.startListening();
    }

    public static class WebSocketHandlingTest implements IWebSocketHandler
    {
        @Override
        public void onMessage( String message, IWebSocketMessageResponder responder )
        {
            System.out.println( "Received message: " + message );
            responder.send( "Hello, World!" );
        }

        @Override
        public void onClose()
        {
            System.out.println( "Connection closed" );
        }

        @Override
        public void onError( Throwable error )
        {
            System.out.println( "Error: " + error.getMessage() );
        }

        @Override
        public void onOpen()
        {
            System.out.println( "Connection opened" );
        }
    }
}
```

With this example code, you can start the server at port `80` and connect to the websocket server at `ws://localhost/`. 
The server will respond to any message with `Hello, World!`.