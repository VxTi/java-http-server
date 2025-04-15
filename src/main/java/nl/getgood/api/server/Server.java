package nl.getgood.api.server;

import nl.getgood.api.Database;
import nl.getgood.api.Logger;
import nl.getgood.api.http.IRequestHandler;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.websocket.IWebSocketHandler;
import nl.getgood.api.middleware.Middleware;
import nl.getgood.api.module.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created on 03/08/2024 at 15:28
 * by Luca Warmenhoven.
 */
public class Server
{

    /**
     * A list containing all registered modules.
     */
    private final List<Module> modules = new ArrayList<>();

    /**
     * Maps containing all registered routes,
     * ranging from static routes to generic routes.
     */
    protected final Map<String, String> staticRoutes = new HashMap<>();
    protected final Map<String, IWebSocketHandler> websocketRoutes = new HashMap<>();
    protected final Map<String, Map<RequestMethod, IRequestHandler>> genericRoutes = new HashMap<>();

    /**
     * A list containing all middlewares.
     */
    protected final List<Middleware> middlewares = new ArrayList<>();

    /**
     * A map containing all server properties.
     * These properties will be applied once the server is started.
     */
    protected final Map<ServerProperty, Object[]> serverProperties = new HashMap<>();

    // The host URL and port of the server.
    protected final String hostUrl;
    protected final int port;

    /**
     * The client acceptor that will be used to accept clients.
     */
    private ClientAcceptor clientAcceptor;

    /**
     * The SHA1 message digest that will be used to hash data.
     * Mainly used for websockets.
     */
    protected MessageDigest SHA1 = null;

    /**
     * The server socket that will be used to accept clients.
     */
    private ServerSocket serverSocket;

    /**
     * The thread pool that will be used to handle client connections.
     */
    protected ThreadPoolExecutor threadPool;

    protected final Logger logger = Logger.getLogger();
    protected String cors = null;
    protected boolean corsEnabled = false;

    private Database queryExecutor = null;

    /**
     * Creates a new API server instance.
     *
     * @param port    The port that the server will be hosted on.
     * @param address The host URL of the server.
     */
    private Server( int port, String address )
    {
        this.port = port;
        this.hostUrl = address;

        try
        {
            this.SHA1 = MessageDigest.getInstance( "SHA-1" );
        }
        catch ( Exception e )
        {
            logger.error( "Failed to initialize SHA1 message digest: " + e.getMessage() );
            this.setProperty( ServerProperty.ALLOW_WEBSOCKET_CONNECTIONS, false );
        }
    }

    /**
     * Creates a new API server instance.
     *
     * @param port The port that the server will be hosted on.
     */
    public static Server create( int port )
    {
        return new Server( port, "localhost" );
    }

    /**
     * Creates a new Server instance from the provided properties file.
     *
     * @param file The file to read the properties from.
     * @return The created server instance.
     */
    public static Server createFromProperties( File file )
    {
        Server server = new Server( 80, "localhost" );
        server.useProperties( file );
        return server;
    }

    /**
     * Creates a new Server instance from the provided properties file.
     *
     * @param relativePath The relative path to the properties file.
     * @return The created server instance.
     */
    public static Server createFromProperties( String relativePath )
    {
        return createFromProperties( new File( relativePath ) );
    }

    /**
     * Creates a new API server instance.
     *
     * @param port    The port that the server will be hosted on.
     * @param address The host URL of the server.
     */
    public static Server create( int port, String address )
    {
        return new Server( port, address );
    }

    /**
     * Adds a module to the API server.
     *
     * @param module The module to add.
     * @return The API server instance.
     */
    public Server useModule( Module module )
    {
        this.modules.add( module );
        List<RegisteredApiRoute> apiHandlers = module.getApiHandlers();

        for ( RegisteredApiRoute apiRoute : apiHandlers )
        {
            Map<RequestMethod, IRequestHandler> handlers =
                    this.genericRoutes.containsKey( apiRoute.getRoute() ) ?
                            this.genericRoutes.get( apiRoute.getRoute() ) : new HashMap<>();

            handlers.put( apiRoute.getRequestMethod(), apiRoute.getRequestHandler() );
            this.genericRoutes.put( apiRoute.getRoute(), handlers );
        }

        return this;
    }

    /**
     * Registers a static route.
     *
     * @param route     The route to register.
     * @param localPath The path to the file to serve.
     * @return The API server instance.
     */
    public Server serveStatic( final String route, String localPath )
    {
        File targetPath = new File( localPath );
        if ( targetPath.isDirectory() && targetPath.canRead() )
        {
            if ( localPath.endsWith( "/" ) )
            {
                localPath = localPath.substring( 0, localPath.length() - 1 );
            }
            this.staticRoutes.put( route, localPath );
        }
        return this;
    }

    /**
     * Adds a middleware to the API server.
     *
     * @param middleware The middleware to add.
     * @return The API server instance.
     */
    public Server use( Middleware middleware )
    {
        this.middlewares.add( middleware );
        return this;
    }

    /**
     * Registers a GET route.
     *
     * @param route          The route to register.
     * @param requestHandler The method to invoke when the route is requested.
     * @return The API server instance.
     */
    public Server get( String route, IRequestHandler requestHandler )
    {
        registerRoute( route, RequestMethod.GET, requestHandler );
        return this;
    }

    /**
     * Registers a POST route.
     *
     * @param route          The route to register.
     * @param requestHandler The method to invoke when the route is requested.
     * @return The API server instance.
     */
    public Server post( String route, IRequestHandler requestHandler )
    {
        registerRoute( route, RequestMethod.POST, requestHandler );
        return this;
    }

    public Server websocket( String route, IWebSocketHandler requestHandler )
    {
        if ( ! this.getProperty( ServerProperty.ALLOW_WEBSOCKET_CONNECTIONS, Boolean.class ) )
            throw new RuntimeException( "Websockets are not enabled on this server." );

        this.websocketRoutes.put( route, requestHandler );
        return this;
    }

    /**
     * Register a route with the given method and handler.
     *
     * @param route   The route to register.
     * @param method  The method to register.
     * @param handler The handler to register.
     */
    private void registerRoute( String route, RequestMethod method, IRequestHandler handler )
    {
        Map<RequestMethod, IRequestHandler> handlerMap = this.genericRoutes.getOrDefault( route, new HashMap<>() );
        handlerMap.put( method, handler );
        this.genericRoutes.put( route, handlerMap );
    }

    /**
     * Method for starting the server.
     * This will create a thread pool with a configured amount of threads,
     * start the server socket and start accepting clients.
     */
    public void startListening()
    {
        // Prevent the server from starting multiple times.
        if ( this.serverSocket != null && ! this.serverSocket.isClosed() )
        {
            logger.error( "Server is already running." );
            return;
        }
        try
        {
            if ( this.getProperty( ServerProperty.USE_DATABASE, Boolean.class ) )
            {
                logger.verbose( "Attempting to establish database connection" );
                this.queryExecutor = new Database(
                        getProperty( ServerProperty.DATABASE_HOST, String.class ),
                        getProperty( ServerProperty.DATABASE_USERNAME, String.class ),
                        getProperty( ServerProperty.DATABASE_PASSWORD, String.class ),
                        getProperty( ServerProperty.DATABASE_NAME, String.class ),
                        getProperty( ServerProperty.DATABASE_MAX_CONNECTIONS, Integer.class )
                );
            }

            this.serverSocket = new ServerSocket( this.port );
            this.clientAcceptor = new ClientAcceptor( this, this.serverSocket );

            this.threadPool = ( ThreadPoolExecutor ) Executors.newFixedThreadPool(
                    this.getProperty( ServerProperty.CONNECTION_CONCURRENCY, 0, Integer.class ) );
            this.corsEnabled = this.getProperty( ServerProperty.CORS_ENABLED, Boolean.class );

            if ( this.corsEnabled )
                this.cors = this.getProperty( ServerProperty.CORS, String.class );

            this.modules.forEach( module -> module.initialize( this, this.queryExecutor ) );

            logger.info( "Server started on " + this.hostUrl + ":" + this.port );
            logger.info( "Registered " + this.genericRoutes.size() + " API route(s)" );
            logger.info( "Registered " + this.middlewares.size() + " middleware(s)" );
            logger.info( "Registered " + this.websocketRoutes.size() + " websocket route(s)" );
            this.staticRoutes.forEach( ( key, value ) -> logger.info( "Serving static route: \"" + key + "\" from \"" + value + "\"" ) );

            this.clientAcceptor.startAccepting();
        }
        catch ( IOException e )
        {
            logger.error( "An error occurred whilst attempting to start the server: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    /**
     * Stops the API server.
     */
    public synchronized void stopListening()
    {
        try
        {
            if ( this.serverSocket != null && ! this.serverSocket.isClosed() )
            {
                this.modules.forEach( Module::close );
                this.serverSocket.close();
                logger.info( "Server stopped." );
            }
        }
        catch ( IOException e )
        {
            logger.error( "An error occurred whilst attempting to stop the server" + e.getMessage() );
        }
    }

    /**
     * Reads the properties from the provided file and sets them.
     * The properties file should be in the format of 'key=value'.
     * The values should be separated by a comma. Spaces in-between are ignored.
     *
     * @param file The file to read the properties from.
     * @return True if the properties were successfully read, false otherwise.
     */
    public boolean useProperties(File file)
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( file)) )
        {
            String line, value;
            ServerProperty property;

            while ( ( line = reader.readLine() ) != null )
            {
                String[] parts = line.split( "=" );
                if ( parts.length != 2 )
                    continue;

                try
                {
                    property = ServerProperty.valueOf( parts[0] );
                }
                catch ( IllegalArgumentException e )
                {
                    continue;
                }

                String[] values = parts[1].split( "," );
                Object[] arguments = new Object[values.length];

                for ( int i = 0; i < values.length; i++ )
                {
                    value = values[i].trim();
                    try
                    {
                        arguments[i] = Integer.parseInt( value );
                    }
                    catch ( NumberFormatException ex1 )
                    {
                        try
                        {
                            arguments[i] = Double.parseDouble( value );
                        }
                        catch ( Exception ex2 )
                        {
                            if ( value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" ) )
                            {
                                arguments[i] = Boolean.parseBoolean( value );
                            }
                            else
                            {
                                arguments[i] = value;
                            }
                        }
                    }
                }
                System.out.println( property + " " + arguments[0] );
                this.serverProperties.put( property, arguments );
            }
            return true;
        }
        catch (IOException e)
        {
            logger.error("An error occurred whilst attempting to read the properties file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the property value in the provided object type.
     * If the property does not exist, the class type is not valid,
     * or the argumentIndex is not valid, the method will return null.
     *
     * @param key           The key of the property.
     * @param argumentIndex The index of the argument to get.
     * @param classType     The class type of the property.
     * @return The property value, or null if the property does not exist.
     */
    protected <T> T getProperty( ServerProperty key, int argumentIndex, Class<T> classType )
    {
        Object[] values = this.getProperty( key );

        if ( values.length <= argumentIndex || argumentIndex < 0 )
            return null;

        if ( ! ( classType.isInstance( values[argumentIndex] ) ) )
            return null;

        return classType.cast( values[argumentIndex] );
    }

    /**
     * Returns the property value in the provided object type at index 0.
     *
     * @param key       The key of the property.
     * @param classType The class type of the property.
     * @return The property value, or null if the property does not exist.
     */
    public <T> T getProperty( ServerProperty key, Class<T> classType )
    {
        return getProperty( key, 0, classType );
    }

    /**
     * Adds a property to the server that can be used on runtime.
     * The property can be retrieved using the 'getProperty' method.
     * Properties can not be changed while the server is running,
     * attempting to do so will result in an IllegalStateException.
     *
     * @param key    The key of the property.
     * @param values The values of the property.
     * @throws IllegalStateException If the server is already running.
     */
    public void setProperty( ServerProperty key, Object... values )
    {
        if ( this.serverSocket != null && ! this.serverSocket.isClosed() )
            throw new IllegalStateException( "Cannot set properties while the server is running." );

        if ( key.getArgumentCount() == values.length )
            this.serverProperties.put( key, values );
    }

    /**
     * Returns an array of property values for the given property,
     * or the default values if the property is not set.
     *
     * @param property The property to get the values of.
     * @return An array of property values.
     */
    public Object[] getProperty( ServerProperty property )
    {
        Object[] values = this.serverProperties.get( property );
        if ( values == null )
            return property.getArguments();

        if ( values.length != property.getArgumentCount() )
        {
            Object[] newValues = new Object[property.getArgumentCount()];
            System.arraycopy( values, 0, newValues, 0, values.length );
            return newValues;
        }

        return values;
    }

    /**
     * Returns the instance of the database query executor.
     */
    public Database getDatabase()
    {
        return this.queryExecutor;
    }
}
