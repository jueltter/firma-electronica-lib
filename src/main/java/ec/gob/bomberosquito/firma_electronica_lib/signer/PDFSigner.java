/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.bomberosquito.firma_electronica_lib.signer;

//import ec.gob.bomberosquito.firma_electronica_lib.utils.PDFSigner;
import ec.gob.bomberosquito.firma_electronica_lib.SignerException;
import ec.gob.bomberosquito.firma_electronica_lib.SignException;
import ec.gob.bomberosquito.firma_electronica_lib.cert.CertificateWrapper;
import ec.gob.bomberosquito.firma_electronica_lib.cert.X509CertificateWrapper;
import ec.gob.bomberosquito.firma_electronica_lib.utils.CertUtils;
import ec.gob.bomberosquito.firma_electronica_lib.utils.FileUtils;
import ec.gob.bomberosquito.firma_electronica_lib.utils.KeyStoreUtilities;
import ec.gob.bomberosquito.firma_electronica_lib.utils.TiempoUtils;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samagua
 */
public final class PDFSigner {

    private static final Logger LOG = Logger.getLogger(PDFSigner.class.getName());    
    
    private final String passwordAsString;
    private final String certPathname;
    private final Certificate[] certificateChain;
    private final KeyStore keyStore;
    private final String defaultAlias;
    private final CertificateWrapper<X509Certificate> certificateWrapper;
    private final PrivateKey key;
    
    public static PDFSigner getDefaultInstance() throws SignerException {
        final String certPathname = PDFSigner.class.getResource("/certificado.p12").getFile();
        final String passwordAsString = "password";
        return new PDFSigner(certPathname, passwordAsString);
    }
    
    public static PDFSigner getInstance(String certPathname, String passwordAsString) throws SignerException {
        return new PDFSigner(certPathname, passwordAsString);
    }
    
    @SuppressWarnings("UseSpecificCatch")
    private PDFSigner(String certPathname, String passwordAsString) throws SignerException {
        this.certPathname = certPathname;
        this.passwordAsString = passwordAsString;
        LOG.log(Level.INFO, "certPathname: {0}", this.certPathname);

        try {
            keyStore = KeyStoreUtilities.getKeystore(this.certPathname, this.passwordAsString.toCharArray());
            defaultAlias = CertUtils.seleccionarAlias(keyStore);
            certificateChain = keyStore.getCertificateChain(defaultAlias);
            Certificate cert = certificateChain[0];
            certificateWrapper = new X509CertificateWrapper(cert);
            key = (PrivateKey) keyStore.getKey(defaultAlias, passwordAsString.toCharArray());
        } catch (Exception ex) {
            throw new SignerException("Unable to create an instance of " + PDFSigner.class.toString(), ex);
        }
    }

    public CertificateWrapper<X509Certificate> getCertificateWrapper() {
        return certificateWrapper;
    }

    public byte[] sign(File file, int page, Point point) throws SignException {        
        return sign(file, point, page, "", "Firma Visible");
    }
    
    public byte[] sign(File file, int page) throws SignException {
        return sign(file, new Point(40, 78), page, "", "Firma Visible");        
    }

    @SuppressWarnings("UseSpecificCatch")
    private byte[] sign(File document, Point point, int page, String razonFirma, String tipoFirma) throws SignException {
        byte[] docByteArry = null;

        try {
            docByteArry = FileUtils.fileConvertToByteArray(document);
        } catch (IOException ex) {
            throw new SignException(ex);
        }

        try {
            Properties config = new Properties();
            config.setProperty("version", "2.3.1");

            Properties params = new Properties();
            params.setProperty("signingLocation", "");
            params.setProperty("signingReason", razonFirma);
            params.setProperty("signTime", TiempoUtils.getFechaHoraServidor());

            if (tipoFirma != null && tipoFirma.equals("Firma Visible")) {

                params.setProperty("0", String.valueOf(page));
                params.setProperty("information1", "QR");
                params.setProperty("", "VALIDAR CON: www.firmadigital.gob.ec\n" + config.getProperty("version"));
                params.setProperty("PositionOnPageLowerLeftX", String.valueOf(point.x));
                params.setProperty("PositionOnPageLowerLeftY", String.valueOf(point.y));
            }
            
            ec.gob.bomberosquito.firma_electronica_lib.utils.PDFSigner signer = new ec.gob.bomberosquito.firma_electronica_lib.utils.PDFSigner();
            return signer.sign(docByteArry, "SHA1withRSA", key, certificateChain, params);
        } catch (Exception ex) {
            throw new SignException(ex);
        }
    }
    
    @SuppressWarnings("UseSpecificCatch")
    public static boolean areCertpathnameAndPasswordCorrect(String certPathname, String passwordAsString) {
        try {            
            Path path = Paths.get(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + Long.toString((new Date()).getTime()) + ".pdf");
            try (InputStream in = PDFSigner.class.getResourceAsStream("/empty-document.pdf")) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            
            File emptyDocument = path.toFile();
            PDFSigner firmador = PDFSigner.getInstance(certPathname, passwordAsString);
            firmador.sign(emptyDocument, 1);
            // uncommet to create a file in the specified outputPathname
            String outputPathname = System.getProperty("java.io.tmpdir") + Long.toString((new Date()).getTime()) + "-signed.pdf";
            try (FileOutputStream fstream = new FileOutputStream(outputPathname)) {
                fstream.write(firmador.sign(emptyDocument, 1));
            }
            
            Files.delete(path);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
    
    
}
