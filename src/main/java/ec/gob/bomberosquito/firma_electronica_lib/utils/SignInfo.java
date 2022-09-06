package ec.gob.bomberosquito.firma_electronica_lib.utils;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class SignInfo {

    private X509Certificate[] certs;
    private String signAlgorithm = null;

    private Date signingTime;

    private byte[] pkcs1 = null;

    public SignInfo(X509Certificate[] chainCert, Date signingTime) {
        if (chainCert == null || chainCert.length == 0 || chainCert[0] == null) {
            throw new IllegalArgumentException("No se ha introducido la cadena de certificacion");
        }

        this.certs = (X509Certificate[]) chainCert.clone();
        this.signingTime = signingTime;
    }

    public X509Certificate[] getCerts() {
        return (this.certs == null) ? null : (X509Certificate[]) this.certs.clone();
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
        String desc = Utils.getCN(this.certs[0]);
        if (this.signingTime != null) {
            desc = desc + " (" + DateFormat.getDateTimeInstance(2, 3).format(this.signingTime) + ")";
        }

        return desc;
    }
}
