# Session Token Validation and Creation

---

Validation of session tokens is a critical part of security for every application.
Therefore, there exists a class that does this for you. This is the `SessionTokenValidator` class.
This class implements the methods `validate` and `newToken`. The `newToken` method is used to create a new session token, 
while the `validate` method is used to validate a session token. 

An example of the validation is shown below:

```java

import nl.getgood.admin.SessionToken;
import nl.getgood.api.Database;

public void myFunction()
{
    // Create a database connection
    Database database = new Database( "host", "username", "password", "database", 10 );
    
    // The session token to validate
    String sessionToken = "test-EphV4fBiB3fPXlmADsrig3Tqs5CfwZ2LR7bBwr0rn1iv3KJz-Wom_p2C-15N2Q9FfWlTzhH4ohghXOJpQ";
    
    // This will return true if the token is valid
    boolean isTokenValid = SessionToken.validate( database, sessionToken );
}

```