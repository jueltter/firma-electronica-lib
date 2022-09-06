/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib.firma;

/**
 *
 * @author samagua
 */
public class SignerException extends Exception {

    public SignerException() {
        super("Unable to create an instance of " + FirmadorPDF.class.toString());
    }

    public SignerException(Throwable cause) {
        super("Unable to create an instance of " + FirmadorPDF.class.toString(), cause);
    }
}
