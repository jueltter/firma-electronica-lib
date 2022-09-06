/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.bomberosquito.firma_electronica_lib.firma;

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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
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
public final class FirmadorPDF {

    private static final Logger LOG = Logger.getLogger(FirmadorPDF.class.getName());    
    
    private final String passwordAsString;
    private final String certPathname;
    private final KeyStore keyStore;
    private final String defaultAlias;
    private final CertificateWrapper<X509Certificate> certificateWrapper;
    
    public static FirmadorPDF getDefaultInstance() throws SignerException {
        final String certPathname = FirmadorPDF.class.getResource("/certificado.p12").getFile();
        final String passwordAsString = "password";
        return new FirmadorPDF(certPathname, passwordAsString);
    }
    
    public static FirmadorPDF getInstance(String certPathname, String passwordAsString) throws SignerException {
        return new FirmadorPDF(certPathname, passwordAsString);
    }
    
    @SuppressWarnings("UseSpecificCatch")
    private FirmadorPDF(String certPathname, String passwordAsString) throws SignerException {
        this.certPathname = certPathname;
        this.passwordAsString = passwordAsString;
        LOG.log(Level.INFO, "certPathname: {0}", this.certPathname);

        try {
            KeyStoreProvider keyStoreProvider = new KeyStoreProvider(new File(this.certPathname));
            keyStore = keyStoreProvider.getKeystore(this.passwordAsString.toCharArray());
            defaultAlias = CertUtils.seleccionarAlias(keyStore);
            Certificate cert = keyStore.getCertificate(defaultAlias);
            certificateWrapper = new X509CertificateWrapper(cert);
        } catch (Exception ex) {
            throw new SignerException(ex);
        }
    }

    public CertificateWrapper<X509Certificate> getCertificateWrapper() {
        return certificateWrapper;
    }

    public byte[] firmar(File temp, int pagina, Point point) throws Exception {        
        return firmar(keyStore, defaultAlias, temp, passwordAsString.toCharArray(), point, pagina, "", "Firma Visible");
    }
    
    public byte[] firmar(File temp, int pagina) throws Exception {
        return firmar(keyStore, defaultAlias, temp, passwordAsString.toCharArray(), new Point(40, 78), pagina, "", "Firma Visible");        
    }

    private byte[] firmar(KeyStore keyStore, String alias, File documento, char[] clave, Point point, int page, String razonFirma, String tipoFirma) throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        byte[] docByteArry = FileUtils.fileConvertToByteArray(documento);
        if (docByteArry.length == 0) {
            //MensajesErrores.fatal("Archivo no valido para la firma");
            return null;
        }
        PDFSigner signer = new PDFSigner();

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
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, clave);
        Certificate[] certChain = keyStore.getCertificateChain(alias);
        return signer.sign(docByteArry, "SHA1withRSA", key, certChain, params);
    }
    
    public static boolean areCertpathnameAndPasswordCorrect(String certPathname, String passwordAsString) {
        try {            
            Path path = Paths.get(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + Long.toString((new Date()).getTime()) + ".pdf");
            try (InputStream in = FirmadorPDF.class.getResourceAsStream("/empty-document.pdf")) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            
            File emptyDocument = path.toFile();
            FirmadorPDF firmador = FirmadorPDF.getInstance(certPathname, passwordAsString);
            firmador.firmar(emptyDocument, 1);
            // uncommet to create a file in the specified outputPathname
            /*String outputPathname = System.getProperty("java.io.tmpdir") + Long.toString((new Date()).getTime()) + "-signed.pdf";
            try (FileOutputStream fstream = new FileOutputStream(outputPathname)) {
                fstream.write(firmador.firmar(emptyDocument, 1));
            }*/
            
            Files.delete(path);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(FirmadorPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
    
    
}
