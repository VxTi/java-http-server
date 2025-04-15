package nl.getgood.api.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.getgood.api.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 03/08/2024 at 16:55
 * by Luca Warmenhoven.
 */
public class Response
{

    private final Map<String, String> headers = new HashMap<>();
    private StatusCode statusCode;
    private ContentType contentType;
    private boolean isSent = false;
    private boolean allowResend = false;
    private Protocol protocol = Protocol.HTTP_1_1;
    private String body;
    private PrintStream streamOut;

    /**
     * Default constructor for a response,
     * this will return a 200 OK response with a text body.
     */
    public Response(PrintStream streamOut)
    {
        this( streamOut, StatusCode.OK, ContentType.TEXT_PLAIN, "");
    }

    /**
     * Constructor for a response with a status code, content type and body.
     *
     * @param statusCode  The status code of the response.
     * @param contentType The content type of the response.
     * @param body        The body of the response.
     */
    public Response(PrintStream streamOut, StatusCode statusCode, ContentType contentType, String body)
    {
        this.streamOut = streamOut;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    public Response(PrintStream streamOut, StatusCode statusCode, boolean allowResend)
    {
        this( streamOut, statusCode, ContentType.TEXT_PLAIN, "");
        this.allowResend = allowResend;
    }

    /**
     * Constructor for a response with a status code.
     *
     * @param statusCode The status code of the response.
     *                   This will return a response with the status code and a text body.
     * @param streamOut  The print stream to send the response to.
     */
    public Response(PrintStream streamOut, StatusCode statusCode)
    {
        this( streamOut, statusCode, ContentType.TEXT_PLAIN, "");
    }

    /**
     * Constructor for a response with a status code and body.
     *
     * @param statusCode The status code of the response.
     * @param streamOut  The print stream to send the response to.
     * @param allowResend Whether the response can be resent.
     */
    public Response( OutputStream streamOut, StatusCode statusCode, boolean allowResend )
    {
        this( new PrintStream( streamOut ), statusCode, allowResend );
    }

    /**
     * Set the status code of the response.
     *
     * @param statusCode The status code of the response.
     */
    public Response status(StatusCode statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Redirect the client to a different location.
     * This will set the status code to 301 and add a location header.
     *
     * @param location The location to redirect the client to.
     */
    public Response redirect(String location)
    {
        this.statusCode = StatusCode.MOVED_PERMANENTLY;
        this.headers.put("Location", location);
        return this;
    }

    /**
     * Set the protocol of the response.
     *
     * @param protocol The protocol of the response.
     */
    public Response protocol(Protocol protocol)
    {
        this.protocol = protocol;
        return this;
    }

    public Response header(String key, String value)
    {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Set the body of the response.
     *
     * @param body The body of the response.
     */
    public Response body(String body)
    {
        this.body = body;
        return this;
    }

    /**
     * Set the body of the response in bytes.
     *
     * @param body The body of the response.
     */
    public Response body(byte[] body)
    {
        this.body = new String(body);
        return this;
    }

    /**
     * Set the body of the response to a JSON object.
     *
     * @param json The JSON object to set the body to.
     */
    public Response json( JsonElement json)
    {
        this.contentType = ContentType.JSON;
        return this.body(json.toString());
    }

    public Response json( JsonObject[] objects )
    {
        this.contentType = ContentType.JSON;
        JsonArray array = new JsonArray();
        for ( JsonObject object : objects )
            array.add( object );
        return this.body( array.toString() );
    }

    /**
     * Set the content type of the response.
     *
     * @param contentType The content type of the response.
     */
    public Response contentType(ContentType contentType)
    {
        this.contentType = contentType;
        return this;
    }


    public StatusCode getStatusCode()
    {
        return statusCode;
    }

    public String getBody()
    {
        return body;
    }

    public ContentType getContentType()
    {
        return contentType;
    }

    @Override
    public String toString()
    {
        return "Response{" +
                "body='" + body + '\'' +
                ", statusCode=" + statusCode +
                ", contentType=" + contentType +
                ", protocol=" + protocol +
                ", allowResend=" + allowResend +
                '}';
    }

    /**
     * Send the response to the client's output stream.
     * This method will only send data to the client if it wasn't already sent,
     * or if the `allowResend` flag is set to true.
     */
    public void send()
    {
        if ( this.isSent == (this.isSent = true) && !this.allowResend)
        {
            Logger.getLogger().error("Attempting to resent already sent response.");
            return;
        }
        try
        {
            if ( this.streamOut == null )
            {
                return;
            }
            // Status header
            this.streamOut.printf("%s %d %s\r\n",
                          this.protocol.getProtocol(),
                          this.statusCode.getCode(),
                          this.statusCode.getMessage()
            );

            this.headers.forEach((key, value) -> this.streamOut.printf("%s: %s\r\n", key, value));

            if ( this.body.length() > 0 )
            {
                this.streamOut.printf( "Content-Type: %s\r\n", this.contentType.getType() );
                this.streamOut.printf( "Content-Length: %d\r\n", this.body.length() );
            }
            this.streamOut.print("\r\n");
            this.streamOut.println(this.getBody());

            this.streamOut.flush();
        }
        catch (Exception e)
        {
            Logger.getLogger().error("Failed to send response: " + e.getMessage());
        }
    }
}
