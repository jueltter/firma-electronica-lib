/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.bomberosquito.firma_electronica_lib.firma;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author samagua
 */
public class FirmadorPDFTest {
    
    public FirmadorPDFTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void validarCertificadoTest() throws SignerException {
        assertEquals(true, FirmadorPDF.areCertpathnameAndPasswordCorrect(FirmadorPDFTest.class.getResource("/certificado.p12").getFile(), "password"));
    }
}
