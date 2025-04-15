package nl.getgood.api.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created on 05/08/2024 at 13:56
 * by Luca Warmenhoven.
 */
public class ClientAcceptor
{

    private final ServerSocket serverSocket;
    private final Server server;

    /**
     * Creates a new client acceptor.
     * This constructor is used to create a new client acceptor.
     * It requires a server instance and a server socket.
     * The server instance is used to handle the client connections.
     * The server socket is used to accept clients.
     *
     * @param server       The server instance.
     * @param serverSocket The server socket.
     */
    protected ClientAcceptor( Server server, ServerSocket serverSocket )
    {
        this.server = server;
        this.serverSocket = serverSocket;
    }

    /**
     * Starts the client acceptor.
     * This method will start accepting clients and create a
     * new ClientConnection object and thread for each client.
     */
    protected void startAccepting()
    {
        if ( this.serverSocket == null || this.serverSocket.isClosed() )
            throw new IllegalStateException( "Server socket is not open" );

        Runnable acceptor = () ->
        {
            while ( ! this.serverSocket.isClosed() )
            {
                acceptClient();
            }
        };

        this.server.threadPool.execute( acceptor );
    }

    /**
     * Starts the client acceptor.
     * This method will start the client acceptor, which will accept clients and create a new ClientConnection for
     * each client.
     */
    protected void acceptClient()
    {
        try
        {
            final Socket clientSocket = this.serverSocket.accept();
            clientSocket.setKeepAlive( true );
            clientSocket.setTcpNoDelay( server.getProperty( ServerProperty.SOCKET_TCP_NO_DELAY, 0, Boolean.class ) );
            ClientConnection clientConnection = new ClientConnection( this.server, clientSocket );
            this.server.threadPool.execute( () ->
                                            {
                                                clientConnection.handleConnection();
                                                clientConnection.closeConnection();
                                            } );
        }
        catch ( IOException e )
        {
            this.server.logger.error( "Failed to accept client", e );
            this.server.logger.errorStack( e );
        }
    }
}
