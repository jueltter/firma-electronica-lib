/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib;

/**
 *
 * @author samagua
 */
public class SignException extends Exception {

    public SignException() {
        super("Unable to sign file");
    }

    public SignException(Throwable cause) {
        super("Unable to sign file", cause);
    }
}
