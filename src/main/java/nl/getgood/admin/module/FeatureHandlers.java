package nl.getgood.admin.module;

import nl.getgood.admin.SessionToken;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;
import nl.getgood.api.http.StatusCode;
import nl.getgood.api.module.Module;
import nl.getgood.api.server.Route;

/**
 * Created on 07/08/2024 at 02:18
 * by Luca Warmenhoven.
 */
public class FeatureHandlers extends Module
{

    private static final String FEATURE_QUERY =
            "SELECT * FROM `StoreFeatures` WHERE `StoreID` = ( SELECT `StoreID` FROM `User` WHERE " +
            "`UserID` = ( SELECT `UserID` FROM `Session` WHERE `SessionUID` = ? ) )";

    /**
     * Handles the feature request.
     * This method will handle the feature request using the `/v1/features` endpoint.
     * The request must contain a sessionId field in the URL parameters.
     * Below is an example of the URL parameters of the request. <br/>
     * URL parameters:
     * <pre>/v1/features?sessionId=session_id`</pre>
     *
     * @param request  The request that contains the URL parameters.
     * @param response The response that will be sent back to the user.
     */
    @Route(route = "/v1/features", method = RequestMethod.GET)
    public void handleFeature( Request request, Response response )
    {
        response.status( StatusCode.BAD_REQUEST );

        if ( ! request.urlParameters.containsKey( "sessionId" ) )
            return;

        String sessionId = request.urlParameters.get( "sessionId" );
        if ( ! SessionToken.validate( this.database, sessionId ) )
        {
            response.status( StatusCode.UNAUTHORIZED );
            return;
        }

        response.json( this.database.execute( FEATURE_QUERY, sessionId ) )
                .status( StatusCode.OK );
    }


}
