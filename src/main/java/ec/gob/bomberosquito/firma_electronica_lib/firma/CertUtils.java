package ec.gob.bomberosquito.firma_electronica_lib.firma;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class CertUtils {

    public static String seleccionarAlias(KeyStore keyStore) {
        String aliasString = null;

        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        if (signingAliases.isEmpty()) {
        }

        if (signingAliases.size() == 1) {
            aliasString = ((Alias) signingAliases.get(0)).getAlias();
        } else {
            Alias alias = (Alias) JOptionPane.showInputDialog(null, "Escoja...", "Certificado para firmar", 3, null, signingAliases
                    .toArray(), signingAliases.get(0));
            if (alias != null) {
                aliasString = alias.getAlias();
            }
        }
        return aliasString;
    }

    public static X509Certificate getCert(KeyStore ks) throws KeyStoreException {
        String alias = seleccionarAlias(ks);
        if (alias != null) {
            return (X509Certificate) ks.getCertificate(alias);
        }

        return null;
    }

    public static X509Certificate getCert(KeyStore ks, String alias) throws KeyStoreException {
        if (alias != null) {
            return (X509Certificate) ks.getCertificate(alias);
        }

        return null;
    }
}
