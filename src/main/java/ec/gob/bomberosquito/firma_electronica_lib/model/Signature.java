/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib.model;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 *
 * @author samagua
 */
public class Signature {

    private static final Logger LOG = Logger.getLogger(Signature.class.getName());
    
    private final X509Certificate cert;
    private final Date signingTime;
    private final String commonName;
    private final String serialNumber;
    private byte[] pkcs1 = null;
    private String signAlgorithm = null;
    
    public Signature(X509Certificate cert, Date signingTime) {
        if (cert == null) {
            throw new IllegalArgumentException("cert is null");
        }

        this.cert = (X509Certificate) cert;
        this.signingTime = signingTime;
        
        final String principal = getPrincipal(cert);
        
        this.commonName = getCommonName(principal);
        
        final String SERIAL_NUMBER_KEY = "SERIALNUMBER";
        
        this.serialNumber = getRDNvalueFromLdapName(SERIAL_NUMBER_KEY, principal);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getCommonName() {
        return commonName;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public Date getSigningTime() {
        return this.signingTime;
    }

    public String getSignAlgorithm() {
        return this.signAlgorithm;
    }

    public void setSignAlgorithm(String algorithm) {
        this.signAlgorithm = algorithm;
    }

    public byte[] getPkcs1() {
        return (this.pkcs1 == null) ? null : (byte[]) this.pkcs1.clone();
    }

    public void setPkcs1(byte[] pkcs1) {
        this.pkcs1 = (pkcs1 == null) ? null : (byte[]) pkcs1.clone();
    }

    @Override
    public String toString() {
        return "SignInfo{" + "signingTime=" + signingTime + ", commonName=" + commonName + '}';
    }
    
    private static String getPrincipal(X509Certificate certificate) {
        if (certificate == null) {
            throw new NullPointerException("certificate is null");
        }
        return certificate.getSubjectX500Principal().toString();
    }
    
    private static String getCommonName(String principal) {
        if (principal == null) {
            throw new NullPointerException("principal is null");
        }
        
        LOG.info(principal);
        
        final String COMMON_NAME_KEY = "cn";
        
        String commonName = getRDNvalueFromLdapName(COMMON_NAME_KEY, principal);
        
        if (commonName != null) {
            return commonName;
        }
        
        final String ORGANIZATION_UNIT_KEY = "ou";
        
        String organizationUnit = getRDNvalueFromLdapName(ORGANIZATION_UNIT_KEY, principal);
        
        if (organizationUnit != null) {
            return organizationUnit;
        }
        
        final int index = principal.indexOf('=');

        if (index == -1) {
            return principal;
        }
        
        final String key = principal.substring(0, index);
        
        return getRDNvalueFromLdapName(key, principal);
    }
    
    private static String getRDNvalueFromLdapName(String rdn, String principal) {
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
