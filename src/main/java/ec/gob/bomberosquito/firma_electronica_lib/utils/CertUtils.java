package ec.gob.bomberosquito.firma_electronica_lib.utils;

import java.security.KeyStore;
import java.util.List;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class CertUtils {

    public static String seleccionarAlias(KeyStore keyStore) {
        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        if (signingAliases.size() != 1) {
            throw new IllegalArgumentException("There are more than one aliases in the keyStore");
        }

        return signingAliases.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The only alias in the keyStore is null"))
                .getAlias();
    }
}
