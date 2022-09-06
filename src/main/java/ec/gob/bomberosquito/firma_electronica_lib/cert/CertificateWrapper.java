/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib.cert;

import java.security.cert.Certificate;
import java.util.Date;

/**
 *
 * @author samagua
 * @param <E>
 */
public interface CertificateWrapper<E extends Certificate> {

    E getCertificate();
    
    Date getExpiryDate();

    boolean isValid();

}
