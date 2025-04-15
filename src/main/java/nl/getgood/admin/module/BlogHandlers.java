package nl.getgood.admin.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nl.getgood.admin.SessionToken;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;
import nl.getgood.api.http.StatusCode;
import nl.getgood.api.module.Module;
import nl.getgood.api.server.Route;

/**
 * Created on 03/08/2024 at 23:19
 * by Luca Warmenhoven.
 */
public class BlogHandlers extends Module
{

    @Route(route = "/v1/blogs", method = RequestMethod.GET)
    public void handleGetBlogs( Request request, Response response )
    {
        response.status( StatusCode.BAD_REQUEST );

        // URL Parameter must contain storeId
        if ( !request.urlParameters.containsKey( "storeId" ))
            return;

        int storeId, postId = -1;
        try
        {
            storeId = Integer.parseInt( request.urlParameters.get( "storeId" ));

            if (request.urlParameters.containsKey( "postId" ))
                postId = Integer.parseInt( request.urlParameters.get( "postId" ));
        }
        catch ( NumberFormatException e )
        {
            return;
        }

        response.status( StatusCode.OK );
        JsonArray array = new JsonArray();
        String query = "SELECT * FROM `Blog` WHERE `StoreID` = ?";
        Object[] params = { storeId };

        // If a post ID is provided, acquire that specific one, otherwise acquire all.
        if ( postId != -1 )
        {
            query += " AND `BlogID` = ?";
            params = new Object[] { postId, storeId };
        }

        for ( JsonObject result : this.database.execute( query, params ) )
            array.add( result );

        response.json(array);
    }

    // TODO: Finish
    @Route(route = "/v1/blogs", method = RequestMethod.POST)
    public void handlePublishNewBlog( Request request, Response response )
    {
        // Request must require session cookie.
        if ( !request.cookies.containsKey("session") ||
                !SessionToken.validate( this.database, request.cookies.get("session") ) )
        {
            response.status( StatusCode.UNAUTHORIZED );
            return;
        }


        String title = request.bodyAsJson().get("title").getAsString();
        String content = request.bodyAsJson().get("content").getAsString();

        this.server.getDatabase()
                .execute( "INSERT INTO `Blog` (`Title`, `Content`) VALUES (?, ?)", title, content );

        response.status( StatusCode.CREATED );
    }
}
