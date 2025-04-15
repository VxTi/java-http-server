package nl.getgood.api.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.getgood.api.server.Server;

import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 03/08/2024 at 16:25
 * by Luca Warmenhoven.
 */
public class Request
{

    public final RequestMethod requestMethod;
    public final URI uri;
    public final String body;
    public final Map<String, String> headers;
    public final Map<String, String> urlParameters;
    public final Map<String, String> cookies;
    public final Server server;
    public final Socket clientSocket;
    public final ContentType contentType;
    public final Protocol protocol;

    /**
     * Creates a new request instance.
     *
     * @param server        The server instance that the request was made to.
     * @param method        The method of the request.
     *                      This is the method that the request was made with.
     *                      For example: "GET" or "POST".
     * @param uri           The uri of the request.
     *                      This is the path that the request was made to.
     *                      For example: "/api/v1/test".
     * @param body          The body of the request.
     *                      This is the body of the request.
     *                      For example: "Hello, World!".
     * @param headers       The headers of the request.
     * @param urlParameters The URL parameters of the request.
     */
    public Request( Server server, Socket clientSocket, RequestMethod method,
                    ContentType contentType, Protocol protocol,
                    URI uri, String body,
                    Map<String, String> headers,
                    Map<String, String> urlParameters,
                    Map<String, String> cookies )
    {
        this.contentType = contentType;
        this.clientSocket = clientSocket;
        this.server = server;
        this.requestMethod = method;
        this.uri = uri;
        this.body = body;
        this.headers = headers;
        this.urlParameters = urlParameters;
        this.cookies = cookies;
        this.protocol = protocol;
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "requestMethod=" + requestMethod +
                ", uri='" + uri + '\'' +
                ", protocol='" + protocol + "'" +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                ", urlParameters=" + urlParameters +
                ", server=" + server +
                ", clientSocket=" + clientSocket +
                ", contentType=" + contentType +
                '}';
    }

    /**
     * Gets the body of the request as a JSON object.
     *
     * @return The body of the request as a JSON object,
     * or null if the body is not a valid JSON object.
     */
    public JsonObject bodyAsJson()
    {
        try
        {
            return JsonParser.parseString( this.body ).getAsJsonObject();
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    /**
     * The Builder class is a static inner class of Request. It follows the Builder design pattern.
     * It is used to create a new Request object using method chaining.
     */
    public static class Builder
    {
        // The HTTP method of the request
        private RequestMethod requestMethod = RequestMethod.GET;
        private URI uri = null;
        private Protocol protocol = Protocol.HTTP_1_1;
        private String body = "";
        private ContentType contentType = ContentType.TEXT_PLAIN;
        private Map<String, String> urlParameters = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private Socket clientSocket;
        private Server server;

        /**
         * Constructor for the Builder class.
         *
         * @param server The server that received the request.
         */
        public Builder( Server server )
        {
            this.server = server;
        }

        /**
         * Sets the HTTP method of the request.
         *
         * @param requestMethod The HTTP method of the request.
         * @return The Builder instance.
         */
        public Builder setRequestMethod( RequestMethod requestMethod )
        {
            this.requestMethod = requestMethod;
            return this;
        }

        /**
         * Sets the content type of the request.
         *
         * @param contentType The content type of the request.
         * @return The Builder instance.
         */
        public Builder setContentType( ContentType contentType )
        {
            this.contentType = contentType;
            return this;
        }

        public Builder setURI( URI uri )
        {
            this.uri = uri;
            return this;
        }

        /**
         * Sets the body of the request.
         *
         * @param body The body of the request.
         * @return The Builder instance.
         */
        public Builder setBody( String body )
        {
            this.body = body;
            return this;
        }

        public Builder setCookie( String key, String value )
        {
            this.cookies.put( key, value );
            return this;
        }

        /**
         * Sets the body of the request.
         *
         * @param body The body of the request.
         * @return The Builder instance.
         */
        public Builder setBody( byte[] body )
        {
            this.body = new String( body );
            return this;
        }

        /**
         * Sets the body of the request.
         *
         * @param body The body of the request.
         * @return The Builder instance.
         */
        public Builder setBody( char[] body )
        {
            this.body = new String( body );
            return this;
        }

        /**
         * Sets the headers of the request.
         *
         * @param headers The headers of the request.
         * @return The Builder instance.
         */
        public Builder setHeaders( Map<String, String> headers )
        {
            this.headers = headers;
            return this;
        }

        /**
         * Sets the URL parameters of the request.
         *
         * @param urlParameters The URL parameters of the request.
         * @return The Builder instance.
         */
        public Builder setUrlParameters( Map<String, String> urlParameters )
        {
            this.urlParameters = urlParameters;
            return this;
        }

        public Builder setProtocol( Protocol protocol )
        {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets the server that received the request.
         *
         * @param server The server that received the request.
         * @return The Builder instance.
         */
        public Builder setServer( Server server )
        {
            this.server = server;
            return this;
        }

        /**
         * Sets the client socket that made the request.
         *
         * @param clientSocket The client socket that made the request.
         * @return The Builder instance.
         */
        public Builder setClientSocket( Socket clientSocket )
        {
            this.clientSocket = clientSocket;
            return this;
        }

        /**
         * Builds a new Request object using the set parameters.
         *
         * @return A new Request object.
         */
        public Request build()
        {
            return new Request( this.server, this.clientSocket, this.requestMethod,
                                this.contentType, this.protocol, this.uri, this.body,
                                this.headers, this.urlParameters, this.cookies );
        }
    }
}
