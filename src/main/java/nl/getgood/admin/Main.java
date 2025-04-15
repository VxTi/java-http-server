package nl.getgood.admin;

import nl.getgood.admin.module.BlogHandlers;
import nl.getgood.admin.module.FeatureHandlers;
import nl.getgood.admin.module.SessionHandlers;
import nl.getgood.api.Logger;
import nl.getgood.api.server.Server;

/**
 * Created on 03/08/2024 at 19:29
 * by Luca Warmenhoven.
 */
public class Main
{

    /**
     * The main method of the application.
     * This method will start the server and register all the necessary handlers.
     *
     * @param args The arguments that are passed to the application.
     */
    public static void main( String[] args )
    {
        Server server;
        if ( args.length > 0 && args[0].startsWith("--config=") )
        {
            String cfgFile = args[0].split("=")[1];
            server = Server.createFromProperties( cfgFile );
        }
        else {
            server = Server.create(80);
        }

        Logger.setVerbose( true );

        server.useModule( new SessionHandlers() );
        server.useModule( new FeatureHandlers() );
        server.useModule( new BlogHandlers()    );

        server.startListening();

        /*CustomerRegistration.registerCustomer(
                new Customer(
                        "Luca",
                        "test@test.com",
                        "5436364", 0, "test"
                ),
                server.getDatabase()
        );*/

    }
}
