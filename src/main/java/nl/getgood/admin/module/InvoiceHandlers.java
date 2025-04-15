package nl.getgood.admin.module;

import nl.getgood.admin.SessionToken;
import nl.getgood.api.http.Request;
import nl.getgood.api.http.RequestMethod;
import nl.getgood.api.http.Response;
import nl.getgood.api.http.StatusCode;
import nl.getgood.api.module.Module;
import nl.getgood.api.server.Route;

/**
 * Created on 07/08/2024 at 03:34
 * by Luca Warmenhoven.
 */
public class InvoiceHandlers extends Module
{

    /**
     * Handles the retrieval of invoices.
     * This method will handle the retrieval of invoices using the `/v1/invoices` endpoint. <br/>
     * Requires: <br/>
     * - storeId URL Parameter to be present (int) <br/>
     * - sessionId URL Parameter to be present (String)
     *
     * @param request The request that contains the URL parameters.
     * @param response The response that will be sent back to the user.
     */
    @Route( route = "/v1/invoices", method = RequestMethod.GET )
    public void handleGetInvoices( Request request, Response response )
    {
        response.status( StatusCode.BAD_REQUEST );

        // URL Parameter must contain storeId
        if ( !request.urlParameters.containsKey( "storeId" ) || !request.urlParameters.containsKey( "sessionId" ))
            return;

        if ( ! SessionToken.validate( this.database, request.urlParameters.get( "sessionId" ) ) )
        {
            response.status( StatusCode.UNAUTHORIZED );
            return;
        }

        int storeId, invoiceId = -1;
        try
        {
            storeId = Integer.parseInt( request.urlParameters.get( "storeId" ) );

            if ( request.urlParameters.containsKey( "invoiceId" ) )
                invoiceId = Integer.parseInt( request.urlParameters.get( "invoiceId" ) );
        }
        catch ( NumberFormatException e )
        {
            return;
        }

        response.status( StatusCode.OK );

        // If an invoice ID is provided, acquire that specific one, otherwise acquire all.
        if ( invoiceId != -1 )
        {
            response.json( this.database
                    .execute( "SELECT * FROM `Invoice` WHERE `InvoiceID` = ? AND `StoreID` = ?", invoiceId, storeId ) );
            return;
        }

        response.json( this.database
                .execute( "SELECT * FROM `Invoice` WHERE `StoreID` = ?", storeId ) );
    }

    // TODO: Implement
    //  !!! IMPORTANT !!! 
    @Route( route = "/v1/invoices", method = RequestMethod.POST )
    public void handlePostInvoice( Request request, Response response )
    {
        response.status( StatusCode.BAD_REQUEST );   
        
        // If the request does not provide a session cookie,
        // or the cookie is invalid, return unauthorized.
        if ( !request.cookies.containsKey("session") ||
                !SessionToken.validate( this.database, request.cookies.get("session") ) )
        {
            response.status( StatusCode.UNAUTHORIZED );
            return;
        }

        String title = request.bodyAsJson().get( "title" ).getAsString();
        String content = request.bodyAsJson().get( "content" ).getAsString();
        

    }
}
