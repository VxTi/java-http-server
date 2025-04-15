package nl.getgood.api.http.websocket;

/**
 * The WebSocket handler.
 * This interface is used to handle WebSocket connections.
 * Created on 05/08/2024 at 23:31
 * by Luca Warmenhoven.
 */
public interface IWebSocketHandler
{
    /**
     * Message handler.
     * This method is called every time a message is received.
     * One can respond to the message by using the responder's `respond` method.
     * An example of this is as followed:
     * <pre>
     * server.websocket((message, responder) -> {
     *     responder.respond("Hello, World!");
     * });
     * </pre>
     *
     * @param incomingMessage The incoming message.
     * @param responder       The responder.
     */
    void onMessage( String incomingMessage, IWebSocketMessageResponder responder );

    /**
     * Called when the connection is opened.
     */
    default void onOpen() { }

    /**
     * Called when the connection is closed.
     */
    default void onClose() { }

    /**
     * Called when an error occurs.
     *
     * @param error The error that occurred.
     */
    default void onError( Exception error ) { }
}
