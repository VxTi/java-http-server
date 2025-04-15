package nl.getgood.api.http;

/**
 * Created on 05/08/2024 at 01:33
 * by Luca Warmenhoven.
 */
public enum Protocol
{
    HTTP_3_0( "HTTP/3.0"),
    HTTP_2_0( "HTTP/2.0"),
    HTTP_1_1( "HTTP/1.1"),
    HTTP_1_0( "HTTP/1.0");

    private final String protocol;

    Protocol(String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Parses a protocol from a string.
     * This method will return the protocol that matches the given string.
     * If no protocol matches the string, HTTP/1.1 will be returned.
     *
     * @param headerPart The string to parse.
     * @return The protocol that matches the string.
     */
    public static Protocol parse( String headerPart )
    {
        for ( Protocol protocol : values() )
        {
            if ( protocol.protocol.equals( headerPart ) )
                return protocol;
        }
        return HTTP_1_1;
    }

    public String getProtocol()
    {
        return protocol;
    }
}
