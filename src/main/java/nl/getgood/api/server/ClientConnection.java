package nl.getgood.api.server;

import nl.getgood.api.Logger;
import nl.getgood.api.http.*;
import nl.getgood.api.http.websocket.IWebSocketHandler;
import nl.getgood.api.http.websocket.IWebSocketMessageResponder;
import nl.getgood.api.http.websocket.WebSocketFrame;
import nl.getgood.api.http.websocket.WebSocketMessageType;
import nl.getgood.api.middleware.Middleware;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 05/08/2024 at 18:17
 * by Luca Warmenhoven.
 */
public class ClientConnection
{

    private static final String ERR_MESSAGE = "<html><head><title>400 Bad Request</title></head><body " +
            "style=\"display:flex;flex-direction:column;align-items:center;width:100vw;\"><h1>400 Bad" +
            " Request</h1><p>Your browser sent a request that this server could not understand.</p></body></html>";

    private final Server server;
    private final Socket clientSocket;
    private final InputStream clientIn;
    private final OutputStream clientOut;
    private final BufferedReader clientReader;
    private final Response BAD_REQUEST_RESPONSE;
    private final String clientAddressStr;
    private final Map<String, String> headers;
    private final Map<String, String> urlParameters;

    private static final Logger logger = Logger.getLogger();

    public ClientConnection( Server source, Socket clientSocket ) throws IOException
    {
        this.server = source;
        this.clientSocket = clientSocket;
        this.clientAddressStr = clientSocket.getInetAddress().toString();
        this.clientIn = clientSocket.getInputStream();
        this.clientOut = clientSocket.getOutputStream();
        this.clientReader = new BufferedReader( new InputStreamReader( clientIn ) );

        this.headers  = new HashMap<>();
        this.urlParameters = new HashMap<>();

        this.BAD_REQUEST_RESPONSE = new Response( clientOut, StatusCode.BAD_REQUEST, true )
                .contentType( ContentType.HTML )
                .body( ERR_MESSAGE );
    }

    /**
     * Handles the connection.
     * This method is called when a client connects to the server.
     * The connection can then be handled by the client connection.
     * This method is also run on a different thread than the other connections,
     * using a thread pool.
     */
    protected void handleConnection()
    {
        try
        {
            PrintStream clientWriter = new PrintStream( clientOut );
            String headerLine = this.clientReader.readLine();

            // If the header is null, the client disconnected
            if ( headerLine == null )
                return;

            String[] headerParts = headerLine.split( " " );

            // Check if the header is valid
            if ( headerParts.length != 3 )
            {
                BAD_REQUEST_RESPONSE.send();
                return;
            }

            RequestMethod requestMethod = RequestMethod.parse( headerParts[0] );
            URI requestUri = URI.create( headerParts[1] );
            Protocol protocol = Protocol.parse( headerParts[2] );

            this.urlParameters.putAll(getQueryParameters( requestUri.getQuery() ));

            Request.Builder requestBuilder = new Request.Builder( this.server )
                    .setRequestMethod( requestMethod )
                    .setURI( requestUri )
                    .setHeaders( headers )
                    .setClientSocket( this.clientSocket )
                    .setUrlParameters( this.urlParameters )
                    .setServer( this.server )
                    .setProtocol( protocol );

            logger.verbose( "Incoming request: " + requestMethod + " " + requestUri + " " + protocol );

            int read;
            String line, boundary = null;
            String uriPath = requestUri.getPath();
            int bodyLength = 0;

            // Start reading the headers
            while ( ( line = this.clientReader.readLine() ) != null && ! line.isEmpty() )
            {
                String[] parts = line.split( ": " );

                // Prevent malformed headers.
                if ( parts.length != 2 )
                {
                    BAD_REQUEST_RESPONSE.send();
                    return;
                }
                else switch ( parts[0].toLowerCase() )
                {
                    case "content-length" ->
                    {
                        try
                        {
                            bodyLength = Integer.parseInt( parts[1] );
                        }
                        catch ( NumberFormatException e )
                        {
                            BAD_REQUEST_RESPONSE.send();
                            return;
                        }
                    }
                    case "cookie" -> {
                        String[] cookies = parts[1].split("; ");
                        for ( String cookie : cookies )
                        {
                            String[] cookieParts = cookie.split( "=" );
                            if ( cookieParts.length == 2 )
                                requestBuilder.setCookie( cookieParts[0], cookieParts[1] );
                        }
                    }
                    case "content-type" ->
                    {
                        String[] contentTypeValueParts = parts[1].split( "; " );
                        if ( contentTypeValueParts[0].startsWith( "multipart/form-data" ) )
                        {
                            String[] boundaryParts = contentTypeValueParts[1].split( "boundary=" );
                            if ( boundaryParts.length == 2 )
                            {
                                boundary = parts[1].split( "boundary=" )[1];
                            }
                            parts[1] = contentTypeValueParts[0];
                        }
                        requestBuilder.setContentType( ContentType.of( parts[1] ) );
                    }
                }
                this.headers.put( parts[0].toLowerCase(), parts[1] );
            }

            // If the client sent a body length, start reading the body.
            if ( bodyLength != 0 )
            {
                char[] body = new char[bodyLength];

                // Ensure the body is read correctly.
                if ( ( read = this.clientReader.read( body ) ) == - 1 || read != bodyLength )
                {
                    BAD_REQUEST_RESPONSE.send();
                    return;
                }
                requestBuilder.setBody( body );
            }

            Request request = requestBuilder.build();
            Response response = new Response( clientWriter );

            /* ----- CORS CHECKS -----
             * If the server has CORS enabled, we want to check if the client is allowed to connect.
             */
            if ( this.server.corsEnabled )
            {
                if ( this.server.cors.equals( "*" ) || this.server.hostUrl.equals( requestUri.getHost() ) )
                {
                    response.header( "Access-Control-Allow-Origin", "*" );
                }
                else
                {
                    server.logger.verbose( "CORS policy does not allow this origin." );
                    response.body( "CORS policy does not allow this origin." )
                            .contentType( ContentType.TEXT_PLAIN )
                            .status( StatusCode.FORBIDDEN )
                            .send();
                    return;
                }
            }

            /* ----- MIDDLEWARE -----
             * Before we start handling the request, we want to check if any middleware is present.
             * If middleware is present, we want to propagate the request through the middleware before
             * we start handling the request.
             */
            for ( Middleware middleware : this.server.middlewares )
            {
                // If the middleware returns false, stop propagating the request.
                if ( ! middleware.handleRequest( request, response ) )
                {
                    logger.verbose( "Middleware stopped request propagation." );
                    response.send();
                    return;
                }
            }

            // If the client requests to upgrade the connection to a websocket connection, check if
            // the required headers are present and if the server has a websocket route for the requested URI.
            if ( this.server.getProperty( ServerProperty.ALLOW_WEBSOCKET_CONNECTIONS, Boolean.class )
                    && headers.containsKey( "connection" ) && headers.containsKey( "upgrade" )
                    && headers.containsKey( "sec-websocket-key" )
                    && headers.get( "connection" ).equalsIgnoreCase( "upgrade" )
                    && headers.get( "upgrade" ).equalsIgnoreCase( "websocket" )
                    && server.websocketRoutes.containsKey( requestUri.getPath() )
                    && server.SHA1 != null )
            {
                upgradeWebsocketConnection( request, response, server.websocketRoutes.get( requestUri.getPath() ) );
                return;
            }

            /* ----- STATIC SERVING -----
             * Whenever a GET request comes in, we want to check whether a static route exists for the requested
             * route, so that we can send the resources to the client.
             */

            if ( this.server.getProperty( ServerProperty.ALLOW_STATIC_FILE_SERVING, Boolean.class )
                    && this.attemptServeStatic( requestMethod, uriPath, response ) )
                return;

            /* ----- API ROUTES -----
             * If no static route exists for the requested route, we want to check if an API route exists.
             * If so, we want to invoke the handler for the requested route.
             */
            if ( ! this.server.genericRoutes.containsKey( uriPath ) )
            {
                logger.verbose( "No route found for " + uriPath );
                response.status( StatusCode.NOT_FOUND );
            }
            else
            {
                // If the route exists but the method is not defined, return a 405.
                if ( ! this.server.genericRoutes.get( uriPath ).containsKey( requestMethod ) )
                {
                    response.status( StatusCode.METHOD_NOT_ALLOWED );
                }
                else
                {
                    IRequestHandler route = this.server.genericRoutes.get( uriPath ).get( requestMethod );
                    try
                    {
                        route.handleRequest( request, response );
                    }
                    catch ( Exception e )
                    {
                        logger.error( "Failed to invoke route \"" + uriPath + "\": " + e.getMessage() );
                        response.status( StatusCode.INTERNAL_SERVER_ERROR );
                    }
                }
            }
            response.send();
        }
        catch ( IOException | IllegalArgumentException e )
        {
            logger.error( "Failed to handle client connection: " + e.getMessage() );
        }
    }

    /**
     * Attempts to serve a static resource.
     * This method will attempt to serve a static resource to the client.
     * If the resource is not found, a 404 response will be sent to the client.
     * The method will return false if no response has been sent. Otherwise, it will return true.
     *
     * @param requestMethod The request method of the client.
     * @param uriPath       The URI path of the requested resource.
     * @param response      The response that will be sent to the client.
     * @return True if the resource was served, false otherwise.
     */
    private boolean attemptServeStatic( RequestMethod requestMethod, String uriPath, Response response )
    {
        if ( this.server.staticRoutes.isEmpty() )
            return false;

        String localStartingPath = null;
        for ( String route : this.server.staticRoutes.keySet() )
        {
            if ( uriPath.startsWith( route ) )
            {
                localStartingPath = this.server.staticRoutes.get( route );
                break;
            }
        }

        if ( localStartingPath != null )
        {
            // If no file is specified, default to index.html.
            String relativeFilePath = uriPath.endsWith( "/" ) ? uriPath.concat( "index.html" ) :
                    uriPath;

            logger.verbose( "Incoming GET for static resource: " + localStartingPath + relativeFilePath );

            File file = new File( localStartingPath + relativeFilePath );

            if ( ! file.exists() || file.isHidden() || file.isDirectory() )
            {
                logger.error( "Failed GET for static resource: " + file.getAbsolutePath() );

                response.body( ERR_MESSAGE )
                        .contentType( ContentType.HTML )
                        .status( StatusCode.NOT_FOUND )
                        .send();
                return true;
            }

            if ( requestMethod == RequestMethod.GET )
            {
                try
                {
                    // Attempt to serve the file to the client.
                    byte[] fileContent = Files.readAllBytes( file.toPath() );
                    response
                            .body( fileContent )
                            .status( StatusCode.OK )
                            .contentType( ContentType.fromFile( file ) )
                            .send();
                }
                catch ( IOException e )
                {
                    response.status( StatusCode.INTERNAL_SERVER_ERROR );
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Upgrades the connection to a websocket connection.
     * This method is called when the client wants to upgrade the connection to a websocket connection.
     */
    private void upgradeWebsocketConnection( Request request, Response response, IWebSocketHandler requestHandler )
    {
        // Generate the websocket accept key.
        String acceptKey = request.headers.get( "sec-websocket-key" ) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        acceptKey = Base64.getEncoder().encodeToString( this.server.SHA1.digest( acceptKey.getBytes() ) );
        logger.verbose( "[WebSocket] [%s] - Upgrading connection", this.clientAddressStr );

        // Send the response to the client.
        response
                .status( StatusCode.SWITCHING_PROTOCOLS )
                .header( "Sec-WebSocket-Accept", acceptKey )
                .header( "Upgrade", "websocket" )
                .header( "Connection", "Upgrade" );
        if ( request.headers.containsKey( "sec-websocket-protocol" ) )
            response.header( "Sec-WebSocket-Protocol", request.headers.get( "sec-websocket-protocol" ) );

        response.send();

        byte[] byteBuffer = new byte[1024];
        int bytesRead;

        final IWebSocketMessageResponder responder = ( message ) ->
        {
            try
            {
                logger.verbose( "Sending websocket frame to client %s", this.clientAddressStr );
                clientOut.write( WebSocketFrame.encode( WebSocketMessageType.TEXT, message ) );
            }
            catch ( IOException e )
            {
                logger.error( "Failed to send websocket frame: " + e.getMessage() );
            }
        };


        // Continue reading the websocket frames until the connection is closed.
        try
        {
            if ( this.clientSocket.isConnected() )
            {
                requestHandler.onOpen();
            }
            while ( ! this.clientSocket.isClosed() && this.clientSocket.isConnected() )
            {
                if ( ( bytesRead = this.clientIn.read( byteBuffer ) ) == - 1 )
                {
                    break;
                }

                logger.verbose( "[WebSocket] [%s] - Read %d byte(s)", this.clientAddressStr, bytesRead );
                WebSocketFrame frame = WebSocketFrame.decode( byteBuffer );
                logger.verbose( "[WebSocket] [%s] - Decoded frame: %s", this.clientAddressStr, frame.getContent() );
                requestHandler.onMessage( frame.getContent(), responder );
            }
        }
        catch ( IOException error )
        {
            logger.error( "[WebSocket] [%s] - Failed to read websocket frame - %s",
                          this.clientAddressStr, error.getMessage() );
        }
        catch ( Exception e )
        {
            logger.error( "[WebSocket] [%s] - Failed to decode websocket frame - %s",
                          this.clientAddressStr, e.getMessage() );
        }
        finally
        {
            requestHandler.onClose();
        }
    }

    /**
     * Closes the connection.
     * This method is called when the connection needs to be closed.
     * This can be because of an error, or because the client disconnected.
     */
    public void closeConnection()
    {
        try
        {
            clientSocket.close();
        }
        catch ( Exception e )
        {
            logger.error( "Failed to close client connection: " + e.getMessage() );
        }
    }

    /**
     * Parses the query parameters from a URI.
     * This method will parse the query parameters from a URI and return them as a map.
     *
     * @param query The query string to parse.
     * @return A map containing the query parameters.
     */
    private Map<String, String> getQueryParameters( String query )
    {
        Map<String, String> parameters = new HashMap<>();

        if ( query == null )
            return parameters;

        for ( String parameter : query.split( "&" ) )
        {
            String[] parts = parameter.split( "=" );
            if ( parts.length == 2 )
                parameters.put( parts[0], parts[1] );
        }

        return parameters;
    }
}
