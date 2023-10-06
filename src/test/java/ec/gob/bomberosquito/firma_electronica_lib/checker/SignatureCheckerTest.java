package ec.gob.bomberosquito.firma_electronica_lib.checker;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import ec.gob.bomberosquito.firma_electronica_lib.SignerException;
import ec.gob.bomberosquito.firma_electronica_lib.model.Signature;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
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
public class SignatureCheckerTest {

    private static final Logger LOG = Logger.getLogger(SignatureCheckerTest.class.getName());
    
    
    
    public SignatureCheckerTest() {
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
    public void validarCertificadoTest() throws SignerException, FileNotFoundException, IOException, Exception {
        Path path = Paths.get(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + Long.toString((new Date()).getTime()) + ".pdf");
        try (InputStream in = SignatureCheckerTest.class.getResourceAsStream("/document-signed.pdf")) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }

        File file = path.toFile();

        byte[] bytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);) {
            fis.read(bytes);
        }

        List<Signature> result = SignatureChecker.getSignatures(bytes);
        LOG.info(result.toString());

        assertEquals(1, result.size());

        Files.delete(path);
    }
}
