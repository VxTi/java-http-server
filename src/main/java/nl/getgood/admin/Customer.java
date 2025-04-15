package nl.getgood.admin;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Created on 06/08/2024 at 17:48
 * by Luca Warmenhoven.
 */
public class Customer
{

    private final String customerName;
    private final String emailAddress;
    private final String customerStoreUid;
    private final int userId;

    // Password hash and salt.
    private final String hashedPassword;

    protected Customer( String customerName, String customerEmail, String storeUid, int userId, String password )
    {
        this.customerName = customerName;
        this.emailAddress = customerEmail;
        this.customerStoreUid = storeUid;
        this.userId = userId;
        this.hashedPassword = BCrypt.hashpw( password, BCrypt.gensalt() );
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getCustomerStoreUid()
    {
        return customerStoreUid;
    }

    public int getUserId()
    {
        return userId;
    }

    public String getPasswordHash()
    {
        return this.hashedPassword;
    }

}
