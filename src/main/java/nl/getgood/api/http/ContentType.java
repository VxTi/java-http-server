package nl.getgood.api.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created on 03/08/2024 at 17:54
 * by Luca Warmenhoven.
 */
public final class ContentType
{

    public static final ContentType JSON = new ContentType( "application/json" );
    public static final ContentType TEXT_PLAIN = new ContentType( "text/plain" );
    public static final ContentType HTML = new ContentType( "text/html" );
    public static final ContentType APPLICATION_XML = new ContentType( "application/xml" );
    public static final ContentType FORM = new ContentType( "application/x-www-form-urlencoded" );
    public static final ContentType MULTIPART = new ContentType( "multipart/form-data" );
    public static final ContentType BINARY = new ContentType( "application/octet-stream" );
    public static final ContentType WEBSOCKET_EVENT = new ContentType( "application/websocket-events" );

    /**
     * Gets the content type of file.
     *
     * @param file The file to get the content type of.
     * @return The content type of the file.
     */
    public static ContentType fromFile( File file )
    {
        ContentType type = ContentType.TEXT_PLAIN;
        try {
            type = new ContentType( Files.probeContentType( file.toPath() ));
        }
        catch ( IOException e ) {}
        return type;
    }

    /**
     * Creates a new content type instance.
     *
     * @param type The type of the content type.
     *             For example: "application/json" or "text/plain".
     * @return The content type instance.
     */
    public static ContentType of( String type )
    {
        return new ContentType( type );
    }

    private final String type;

    private ContentType( String type )
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "ContentType{type='" + type + "}";
    }
}
