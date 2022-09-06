package ec.gob.bomberosquito.firma_electronica_lib.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.w3c.dom.Node;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class Utils {

    public static InputStream loadFile(URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("Se ha pedido el contenido de una URI nula");
        }

        if (uri.getScheme().equals("file")) {

            String path = uri.getSchemeSpecificPart();
            if (path.startsWith("//")) {
                path = path.substring(2);
            }
            return new FileInputStream(new File(path));
        }

        InputStream tmpStream = new BufferedInputStream(uri.toURL().openStream());
        byte[] tmpBuffer = getDataFromInputStream(tmpStream);
        return new ByteArrayInputStream(tmpBuffer);
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static byte[] getDataFromInputStream(InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBytes;
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }

    public static String getUID(X509Certificate c) {
        if (c == null) {
            return null;
        }
        return getUID(c.getSubjectX500Principal().toString());
    }

    public static String getUID(String principal) {
        if (principal == null) {
            return null;
        }

        String rdn = getRDNvalueFromLdapName("uid", principal);
        if (rdn != null) {
            return rdn;
        }

        int i = principal.indexOf('=');
        if (i != -1) {
            Logger.getLogger(Utils.class.getName()).warning("No se ha podido obtener el UID, se devolvera el fragmento mas significativo");

            return getRDNvalueFromLdapName(principal.substring(0, i), principal);
        }

        Logger.getLogger(Utils.class.getName()).warning("Principal no valido, se devolvera la entrada");
        return principal;
    }

    public static String getCN(X509Certificate c) {
        if (c == null) {
            return null;
        }
        return getCN(c.getSubjectX500Principal().toString());
    }

    public static String getCN(String principal) {
        if (principal == null) {
            return null;
        }

        String rdn = getRDNvalueFromLdapName("cn", principal);
        if (rdn == null) {
            rdn = getRDNvalueFromLdapName("ou", principal);
        }

        if (rdn != null) {
            return rdn;
        }

        int i = principal.indexOf('=');
        if (i != -1) {
            Logger.getLogger(Utils.class.getName()).warning("No se ha podido obtener el Common Name ni la Organizational Unit, se devolvera el fragmento mas significativo");

            return getRDNvalueFromLdapName(principal.substring(0, i), principal);
        }

        Logger.getLogger(Utils.class.getName()).warning("Principal no valido, se devolvera la entrada");
        return principal;
    }

    public static String getRDNvalueFromLdapName(String rdn, String principal) {
        int offset1 = 0;

        while ((offset1 = principal.toLowerCase(Locale.US).indexOf(rdn.toLowerCase(), offset1)) != -1) {
            if (offset1 > 0 && principal.charAt(offset1 - 1) != ',' && principal.charAt(offset1 - 1) != ' ') {
                offset1++;

                continue;
            }
            offset1 += rdn.length();
            while (offset1 < principal.length() && principal.charAt(offset1) == ' ') {
                offset1++;
            }

            if (offset1 >= principal.length()) {
                return null;
            }

            if (principal.charAt(offset1) != '=') {
                continue;
            }

            offset1++;
            while (offset1 < principal.length() && principal.charAt(offset1) == ' ') {
                offset1++;
            }

            if (offset1 >= principal.length()) {
                return "";
            }

            if (principal.charAt(offset1) == ',') {
                return "";
            }
            if (principal.charAt(offset1) == '"') {
                offset1++;
                if (offset1 >= principal.length()) {
                    return "";
                }

                int offset2 = principal.indexOf('"', offset1);
                if (offset2 == offset1) {
                    return "";
                }
                if (offset2 != -1) {
                    return principal.substring(offset1, offset2);
                }
                return principal.substring(offset1);
            }

            int offset2 = principal.indexOf(',', offset1);
            if (offset2 != -1) {
                return principal.substring(offset1, offset2).trim();
            }
            return principal.substring(offset1).trim();
        }

        return null;
    }

    /*public static X509Certificate getCertificate(Node certificateNode) {
        return createCert(certificateNode.getTextContent().trim().replace("\r", "").replace("\n", "").replace(" ", "")
                .replace("\t", ""));
    }*/

    public static X509Certificate createCert(String b64Cert) {
        X509Certificate cert;
        if (b64Cert == null || b64Cert.isEmpty()) {
            Logger.getLogger(Utils.class.getName()).severe("Se ha proporcionado una cadena nula o vacia, se devolvera null");
            return null;
        }

        try (InputStream isCert = new ByteArrayInputStream(Base64.getDecoder().decode(b64Cert))) {
            cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(isCert);
            try {
                isCert.close();
            } catch (IOException e) {
                Logger.getLogger(Utils.class.getName()).log(Level.WARNING, "Error cerrando el flujo de lectura del certificado: {0}", e);
            }
        } catch (Exception e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "No se pudo decodificar el certificado en Base64, se devolvera null: {0}", e);
            return null;
        }
        return cert;
    }

    public static Date getSignTime(String fechaHora) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        try {
            TemporalAccessor accessor = timeFormatter.parse(fechaHora);
            return Date.from(Instant.from(accessor));
        } catch (DateTimeParseException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "La fecha indicada (''{0}'') como momento de firma para PDF no sigue el patron ISO-8601: {1}", new Object[]{fechaHora, e});

            return new Date();
        }
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
        return calendar;
    }

    public static boolean esValido(X509Certificate cert, Date signingTime) {
        return (!signingTime.before(cert.getNotBefore()) && !signingTime.after(cert.getNotAfter()));
    }

    public static String validarFirma(Calendar fechaDesde, Calendar fechaHasta, Calendar fechaFirmado, Calendar fechaRevocado) {
        String retorno = "Válida";
        if (fechaFirmado.compareTo(fechaDesde) >= 0 && fechaFirmado.compareTo(fechaHasta) <= 0) {
            if (fechaRevocado != null && fechaRevocado.compareTo(fechaFirmado) <= 0) {
                retorno = "Inválida";
            }
        } else {
            retorno = "Inválida";
        }
        return retorno;
    }

    public static boolean verifySignature(X509Certificate certificate, X509Certificate rootCertificate) throws InvalidKeyException {
        PublicKey publicKeyForSignature = rootCertificate.getPublicKey();

        try {
            certificate.verify(publicKeyForSignature);
            return true;
        } catch (InvalidKeyException | java.security.cert.CertificateException | java.security.NoSuchAlgorithmException | java.security.NoSuchProviderException | SignatureException e) {

            System.out.println("\n\tSignature verification of certificate having distinguished name \n\t'" + certificate
                    .getSubjectX500Principal() + "'\n\twith certificate having distinguished name (the issuer) \n\t'" + rootCertificate
                            .getSubjectX500Principal() + "'\n\tfailed. Expected issuer has distinguished name \n\t'" + certificate
                            .getIssuerX500Principal() + "' (" + e.getClass().getSimpleName() + ")");

            return false;
        }
    }
}
