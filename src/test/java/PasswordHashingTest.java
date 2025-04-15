import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Created on 06/08/2024 at 18:19
 * by Luca Warmenhoven.
 */
public class PasswordHashingTest
{


    @Test
    public void testPasswordHashComparison()
    {
        String password = "test password";
        String hash = BCrypt.hashpw( password, BCrypt.gensalt() );

        assert BCrypt.checkpw( password, hash );
    }
}
