/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib.cert;

import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 *
 * @author samagua
 */
public class X509CertificateWrapper implements CertificateWrapper<X509Certificate> {

    private final X509Certificate certificate;

    public X509CertificateWrapper(Certificate cert) {
        if (cert == null || !(cert instanceof X509Certificate)) {
            throw new IllegalArgumentException("cert is null or is not an instance of X509Certificate");
        }
        certificate = (X509Certificate) cert;
    }

    @Override
    public Date getExpiryDate() {
        return certificate.getNotAfter();
    }

    @Override
    public boolean isValid() {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException ex) {
            return false;
        }
        return true;
    }

    @Override
    public X509Certificate getCertificate() {
        return certificate;
    }
}
