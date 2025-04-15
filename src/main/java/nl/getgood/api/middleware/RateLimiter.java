package nl.getgood.api.middleware;

import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;
import nl.getgood.api.http.StatusCode;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 04/08/2024 at 18:41
 * by Luca Warmenhoven.
 */
public class RateLimiter implements Middleware
{
    private final int maxRequests;
    private final int timeFrameMs;
    private final boolean skipGetRequests;

    private final Map<InetAddress, RateLimitEntry> rateLimitMap = new HashMap<>();

    /**
     * Creates a new RateLimiter middleware.
     * This middleware can be used to limit the amount of requests that can be made in a given time frame.
     * @param maxRequests The maximum amount of requests that can be made in the given time frame.
     * @param timeFrameMs The time frame in milliseconds.
     */
    public RateLimiter( int maxRequests, int timeFrameMs )
    {
        this( maxRequests, timeFrameMs, false);
    }

    /**
     * Creates a new RateLimiter middleware.
     * This middleware can be used to limit the amount of requests that can be made in a given time frame.
     * @param maxRequests The maximum amount of requests that can be made in the given time frame.
     * @param timeFrameMs The time frame in milliseconds.
     * @param skipGetRequests Whether to skip GET requests.
     */
    public RateLimiter( int maxRequests, int timeFrameMs, boolean skipGetRequests )
    {
        this.maxRequests = maxRequests;
        this.timeFrameMs = timeFrameMs;
        this.skipGetRequests = skipGetRequests;
    }


    @Override
    public boolean handleRequest( Request request, Response response )
    {
        if ( skipGetRequests && request.requestMethod == RequestMethod.GET )
            return true;

        InetAddress clientAddress = request.clientSocket.getInetAddress();
        if ( clientAddress == null )
            return false;

        if ( !rateLimitMap.containsKey( clientAddress )) {
            rateLimitMap.put( clientAddress, new RateLimitEntry( clientAddress ) );
        } else {
            RateLimitEntry entry = rateLimitMap.get( clientAddress );

            long currentTime = System.currentTimeMillis();

            if ( currentTime - entry.lastRequestTime > timeFrameMs ) {
                entry.requestsSent = 0;
                entry.lastRequestTime = currentTime;
            }
            entry.requestsSent++;
            if ( entry.requestsSent > maxRequests ) {
                response.status( StatusCode.TOO_MANY_REQUESTS);
                return false;
            }
        }
        return true;
    }

    private static class RateLimitEntry
    {
        public final InetAddress requestAddress;
        public int requestsSent = 0;
        public long lastRequestTime = System.currentTimeMillis();

        RateLimitEntry(InetAddress requestAddress)
        {
            this.requestAddress = requestAddress;
        }
    }
}
