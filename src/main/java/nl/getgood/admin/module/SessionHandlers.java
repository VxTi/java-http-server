package nl.getgood.admin.module;

import com.google.gson.JsonObject;
import nl.getgood.admin.SessionToken;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;
import nl.getgood.api.http.StatusCode;
import nl.getgood.api.module.Module;
import nl.getgood.api.server.Route;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigInteger;

/**
 * Created on 03/08/2024 at 18:45
 * by Luca Warmenhoven.
 */
public class SessionHandlers extends Module
{

    private static final long SESSION_EXPIRATION_TIME_SECONDS = 60 * 60; // 1 hour
    private static final String SQL_HASH_FROM_EMAIL = 
            "SELECT `Hash` FROM `User` WHERE `User`.`Email` = ?";
    private static final String SQL_SESSION_TIME_FROM_EMAIL = 
            "SELECT `SessionUID`, `CreationTime` FROM `Session` WHERE `UserID` = " +
                    "(SELECT `UserID` FROM `User` WHERE `User`.`Email` = ?)";
    private static final String SQL_INSERT_SESSION =
            "INSERT INTO `Session` (`SessionUID`, `CreationTime`, `UserID`) VALUES (?, ?, " +
                    "(SELECT `UserID` FROM `User` WHERE `Email` = ?))";

    /**
     * Handles the authentication of a user.
     * This method will handle the authentication of a user using the `/v1/authenticate` endpoint.
     * The request body must be of type JSON and contain a type field. <br/>
     * If the type field is `session`, the request must contain a cookie with the session id. <br/>
     * If the type field is `credentials`, the body must contain a username and password field. <br/>
     * Below are examples of the body of the request. <br/>
     * Authentication with credentials:
     * <pre>
     * {
     *     "type": "credentials",
     *     "username": "email",
     *     "password": "password"
     * }
     * </pre>
     * <p>
     * Authentication with session:
     * <pre>
     * {
     *     "type": "session"
     * }
     * </pre>
     *
     * @param request  The request that contains the body of the request.
     * @param response The response that will be sent back to the user.
     */
    @Route(route = "/v1/authenticate", method = RequestMethod.POST)
    public void handleVerify( Request request, Response response )
    {
        // Do database fetching here

        JsonObject body = request.bodyAsJson();
        var responseObj = new JsonObject();
        response.status( StatusCode.BAD_REQUEST );

        // If the body is null or does not contain a type, return a bad request.
        if ( body == null || ! body.has( "type" ) )
            return;

        boolean valid;

        switch ( body.get( "type" ).getAsString() )
        {
            case "session" ->
            {
                // Use cookies
                if ( ! request.cookies.containsKey( "session" ) )
                    return;

                valid = SessionToken.validate( this.database, request.cookies.get("session") );
            }
            case "credentials" ->
            {
                if ( ! body.has( "username" ) || ! body.has( "password" ) )
                    return;

                valid = validateUserCredentials( body.get( "username" ).getAsString(),
                                                 body.get( "password" ).getAsString() );
            }
            default -> { return; }
        }
        responseObj.addProperty( "authorized", valid );
        response.status( StatusCode.OK )
                .json( responseObj );
    }

    /**
     * Handles the creation of a new session.
     * This method will handle the creation of a new session using the `/v1/session` endpoint.
     * The request body must be of type JSON and contain an email and password field. <br/>
     * Below is an example of the body of the request. <br/>
     * <pre>
     * {
     *     "username": "email",
     *     "password": "password"
     * }
     * </pre>
     *
     * If the authentication was successful, the server will set a cookie with the session id.
     *
     * @param request  The request that contains the body of the request.
     * @param response The response that will be sent back to the user.
     */
    @Route(route = "/v1/session", method = RequestMethod.POST)
    public void handleSessionValidation( Request request, Response response )
    {
        response.status( StatusCode.BAD_REQUEST ); // default response

        JsonObject content = request.bodyAsJson();

        // stop if the required properties are not present for authentication
        if ( content == null || ! content.has( "username" ) || ! content.has( "password" ) )
            return;

        // If the request contains a cookie containing the session token,
        // we can verify the session token.
        if ( request.cookies.containsKey("session"))
        {
            // If the session token is valid, return OK
            if ( SessionToken.validate( this.database, request.cookies.get("session") ) )
            {
                response.status( StatusCode.OK );
                return;
            }
        }

        final String email = content.get( "username" ).getAsString();
        final String pass = content.get( "password" ).getAsString();

        // First, we have to check whether the credentials are right.
        if ( validateUserCredentials( email, pass ) )
        {
            // First, check if there exists a session
            JsonObject[] result = this.database.execute( SQL_SESSION_TIME_FROM_EMAIL, email );

            response.status( StatusCode.OK );
            String sessionUid = null;

            // Check if session exists in database
            if ( result.length > 0 )
            {
                // Check if the time is right
                BigInteger time = result[0].get( "CreationTime" ).getAsBigInteger();
                long currentTime = System.currentTimeMillis() / 1000;

                if ( currentTime - time.longValue() < SessionToken.SESSION_EXPIRATION_TIME_SECONDS )
                    sessionUid = result[0].get( "SessionUID" ).getAsString();

            }

            // If the session does not exist, create a new one
            if ( sessionUid == null )
            {
                sessionUid = SessionToken.newToken();

                this.database
                        .execute( SQL_INSERT_SESSION, sessionUid, System.currentTimeMillis() / 1000, email );

            }
            // Set the session cookie
            response.header("Set-Cookie", "session=" + sessionUid + "; Path=/; HttpOnly; Secure; SameSite=Strict");
        }
    }

    /**
     * Validates whether the given credentials are valid.
     * This is done by checking whether the email address exists in the database,
     * and if so, checking whether the generated hash by the provided password is the same
     * as the one in the database.
     *
     * @param emailAddress The email to check
     * @param password     The password to check
     * @return Whether the credentials are valid or not.
     */
    private boolean validateUserCredentials( String emailAddress, String password )
    {
        JsonObject[] result = this.database.execute( SQL_HASH_FROM_EMAIL, emailAddress );

        boolean faulty = result.length != 1 || ! result[0].has( "Hash" );

        String hash = faulty ? "" : result[0].get( "Hash" ).getAsString();
        
        return BCrypt.checkpw( password, hash );
    }
}
