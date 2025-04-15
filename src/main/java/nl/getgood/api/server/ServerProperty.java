package nl.getgood.api.server;

/**
 * Created on 06/08/2024 at 15:55
 * by Luca Warmenhoven.
 */
public enum ServerProperty
{
    /*
     * Server properties with their default values.
     */
    PORT(80),
    HOST("localhost"),
    CORS("*"),
    CORS_ENABLED(false),
    CONNECTION_CONCURRENCY(10),
    ALLOW_WEBSOCKET_CONNECTIONS( true),
    ALLOW_DUPLICATE_ROUTES(false),
    ALLOW_STATIC_FILE_SERVING( true ),
    USE_DATABASE(false),
    DATABASE_USERNAME("root"),
    DATABASE_PASSWORD(""),
    DATABASE_MAX_CONNECTIONS( 10 ),
    DATABASE_HOST( "localhost" ),
    DATABASE_NAME(""),
    DATABASE_PORT( 3306 ),

    SOCKET_TCP_NO_DELAY(true);

    private final Object[] arguments;

    ServerProperty(Object... arguments)
    {
        this.arguments = arguments;
    }

    public int getArgumentCount()
    {
        return this.arguments.length;
    }

    public Object[] getArguments()
    {
        return this.arguments;
    }
}
