package nl.getgood.admin;

import com.google.gson.JsonObject;
import nl.getgood.api.Database;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * This class is used to validate a session token.
 * It will check if the session token is valid and has not expired.
 * Created on 07/08/2024 at 02:19
 * by Luca Warmenhoven.
 */
public class SessionToken
{

    public static final long SESSION_EXPIRATION_TIME_SECONDS = 60 * 60; // 1 hour
    private static final int SESSION_TOKEN_BYTE_LENGTH = 64;

    private static final String SQL_QUERY = "SELECT `SessionUID`, `CreationTime` FROM `Session` WHERE `SessionUID` = ?";

    /**
     * Validates a session token.
     * This method will check if the session token is valid and has not expired.
     *
     * @param database   The database query executor.
     * @param sessionUid The session token to validate.
     * @return True if the session token is valid and has not expired, false otherwise.
     */
    public static boolean validate( Database database, String sessionUid )
    {
        JsonObject[] results = database.execute( SQL_QUERY, sessionUid);

        if ( results.length == 0 )
            return false;

        BigInteger time = results[0].get( "CreationTime" ).getAsBigInteger();

        // If for some reason the time is null, return false
        if ( time == null )
            return false;

        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime - time.longValue() < SESSION_EXPIRATION_TIME_SECONDS;
    }

    /**
     * Generates a new session token.
     * This method will generate a new session token using a secure random number generator,
     * which is then encoded using Base64 encoding. This ensures that the session token is unique,
     * cryptographically secure and URL safe.
     *
     * @return The generated session token.
     */
    public static String newToken()
    {
        SecureRandom rand = new SecureRandom();
        byte[] randomBytes = new byte[SESSION_TOKEN_BYTE_LENGTH];
        rand.nextBytes( randomBytes );
        return Base64.getUrlEncoder().withoutPadding().encodeToString( randomBytes );
    }

    /**
     * Retrieves the store UID from a session token.
     * This method will retrieve the store UID from a session token.
     *
     * @param database   The database query executor.
     * @param sessionUid The session token to retrieve the store UID from.
     * @return The store UID if the session token is valid, null otherwise.
     */
    public static String getStoreUID(Database database, String sessionUid)
    {
        JsonObject[] results = database.execute("SELECT `StoreID` FROM `Session` WHERE `SessionID` = ?", sessionUid);

        if (results.length == 0)
            return null;

        return results[0].get("StoreID").getAsString();
    }

}
