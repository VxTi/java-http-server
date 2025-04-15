import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.getgood.api.server.Server;
import nl.getgood.admin.module.SessionHandlers;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;

/**
 * Created on 03/08/2024 at 19:32
 * by Luca Warmenhoven.
 */

public class VerifyUserTokenServerTest
{

    Server server = null;

    @Test
    public void testVerifyUserToken() throws IOException
    {
        int port = 8080;
        server = Server.create( port );
        server.useModule( new SessionHandlers() );
        server.startListening();
        final String url = "/v1/authenticate";

        System.out.println( "Sending request to http://127.0.0.1:" + port + "/v1/verify" );

        URLConnection connection = URI.create( "http://127.0.0.1:" + port + url)
                .toURL()
                .openConnection();
        connection.setRequestProperty( "Content-Type", "application/json" );
        connection.setDoOutput( true );
        connection.setDoInput( true );

        InputStream inputStream = connection.getInputStream();


        // Read the response here.
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
        StringBuilder response = new StringBuilder();
        String line;
        while ( ( line = bufferedReader.readLine() ) != null )
        {
            response.append( line );
        }

        inputStream.close();

        JsonObject json = JsonParser.parseString( response.toString() ).getAsJsonObject();
        assert json.get( "valid" ).getAsBoolean();

        if ( server != null ) server.stopListening();
    }

}
