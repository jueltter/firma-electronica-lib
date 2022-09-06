package ec.gob.bomberosquito.firma_electronica_lib.firma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class KeyStoreProvider {

    private final File keyStoreFile;

    public KeyStoreProvider(File keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public KeyStoreProvider(String keyStoreFile) {
        this.keyStoreFile = new File(keyStoreFile);
    }

    public KeyStore getKeystore() throws KeyStoreException {
        return getKeystore(null);
    }

    public KeyStore getKeystore(char[] password) throws KeyStoreException {
        InputStream input = null;
        try {
            input = new FileInputStream(this.keyStoreFile);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(input, password);
            return keyStore;
        } catch (FileNotFoundException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchAlgorithmException | java.security.cert.CertificateException | IOException e) {
            throw new KeyStoreException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.getLogger(KeyStoreProvider.class.getName()).warning(e.getMessage());
                }
            }
        }
    }
}
