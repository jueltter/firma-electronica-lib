package ec.gob.bomberosquito.firma_electronica_lib.firma;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BouncyCastleUtils {

    public static void initializeBouncyCastle() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Security.addProvider(new BouncyCastleProvider());
            return null;
        });
    }
}
