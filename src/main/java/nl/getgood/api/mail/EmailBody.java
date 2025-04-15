package nl.getgood.api.mail;

import java.io.File;
import java.util.List;

/**
 * Created on 05/08/2024 at 14:08
 * by Luca Warmenhoven.
 */
public class EmailBody
{

    public enum BodyType {
        TEXT, HTML
    }

    private final List<File> attachments;
    private final String body;
    private final BodyType type;

    /**
     * Create a new email body
     * @param body The body of the email
     * @param type The type of the body
     */
    public EmailBody(String body, BodyType type, File ... attachments)
    {
        this.body = body;
        this.type = type;
        this.attachments = List.of(attachments);
    }

    /**
     * Get the body of the email
     * @return The body of the email
     */
    public String getBody()
    {
        return body;
    }

    /**
     * Get the type of the body
     * @return The type of the body
     */
    public BodyType getType()
    {
        return type;
    }

    /**
     * Get the attachments of the email
     * @return The attachments of the email
     */
    public List<File> getAttachments()
    {
        return attachments;
    }

}
