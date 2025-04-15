package nl.getgood.admin;

import nl.getgood.api.Database;

/**
 * Created on 06/08/2024 at 17:47
 * by Luca Warmenhoven.
 */
public class CustomerRegistration
{

    private static final String updateQuery =
            "INSERT INTO `User` (`UserID`, `Email`, `Hash`) VALUES (?, ?, ?)";

    public static void registerCustomer( Customer customer, Database executor )
    {
        executor.execute( updateQuery,
                          customer.getUserId(),
                          customer.getEmailAddress(),
                          customer.getPasswordHash()
        );
    }

}
