package nl.getgood.api.mail;

import java.util.Properties;

/**
 * Created on 05/08/2024 at 14:08
 * by Luca Warmenhoven.
 */
public final class EmailTransporter
{
    private final String host;
    private final String hostEmail;
    private final String password;
    private final Properties properties = System.getProperties();
    /**
     * Create a new email transporter
     *
     * @param host     The host of the email server
     * @param email The username of the email server
     * @param password The password of the email server
     */
    private EmailTransporter( String host, String email, String password )
    {
        this.host = host;
        this.hostEmail = email;
        this.password = password;
        this.properties.setProperty( "mail.smtp.host", host );
    }

    public static EmailTransporter createTransporter( String host, String username, String password )
    {
        return new EmailTransporter( host, username, password );
    }

    public EmailTransporter property( String key, String value )
    {
        this.properties.setProperty( key, value );
        return this;
    }

    public void sendEmail( String recipient, String subject, EmailBody body )
    {
        // Send the email
    }
}
