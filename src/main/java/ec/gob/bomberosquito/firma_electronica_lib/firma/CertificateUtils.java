package ec.gob.bomberosquito.firma_electronica_lib.firma;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class CertificateUtils {

    public static X509Certificate certificateFromByteArray(byte[] bytes) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            return null;
        }
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
            System.out.println("No se ha podido obtener el Common Name ni la Organizational Unit, se devolvera el fragmento mas significativo");

            return getRDNvalueFromLdapName(principal.substring(0, i), principal);
        }

        System.out.println("Principal no valido, se devolvera la entrada");
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
}
