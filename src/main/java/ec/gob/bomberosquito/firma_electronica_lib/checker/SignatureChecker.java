/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.bomberosquito.firma_electronica_lib.checker;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import ec.gob.bomberosquito.firma_electronica_lib.model.Signature;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//import java.security.Provider;
//import java.security.Security;
//import java.util.Enumeration;

/**
 *
 * @author samagua
 */
public class SignatureChecker {
    
    private static final PdfName PDFNAME_ETSI_RFC3161 = new PdfName("ETSI.RFC3161");
    private static final PdfName PDFNAME_DOCTIMESTAMP = new PdfName("DocTimeStamp");
    
    public static List<Signature> getSignatures(byte[] document) throws IOException, ReflectiveOperationException {
        PdfReader pdfReader = new PdfReader(document);

        final AcroFields acroFields = pdfReader.getAcroFields();
        
        class Presignature {
            private String name;
            private PdfDictionary dictionary;
            private PdfPKCS7 pkcs7;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public PdfDictionary getDictionary() {
                return dictionary;
            }

            public void setDictionary(PdfDictionary dictionary) {
                this.dictionary = dictionary;
            }

            public PdfPKCS7 getPkcs7() {
                return pkcs7;
            }

            public void setPkcs7(PdfPKCS7 pkcs7) {
                this.pkcs7 = pkcs7;
            }
        }
        
        List<String> signatureNames = acroFields.getSignatureNames();
        
//        Provider provider[] = Security.getProviders();
//        for (Provider pro : provider) {
//            System.out.println(pro);
//            for (Enumeration e = pro.keys(); e.hasMoreElements();) {
//                System.out.println("\t" + e.nextElement());
//            }
//
//        }
      
        List<Presignature> signatures = signatureNames.stream()
                .map(obj -> {
                    String signatureName = (String) obj;
                    PdfDictionary pdfDictionary = acroFields.getSignatureDictionary(signatureName);
                    PdfPKCS7 pkcs7 = acroFields.verifySignature(signatureName);

                    Presignature signature = new Presignature();
                    signature.setName(signatureName);
                    signature.setDictionary(pdfDictionary);
                    signature.setPkcs7(pkcs7);
                    return signature;
                })
                .filter(obj -> !PDFNAME_ETSI_RFC3161.equals(obj.getDictionary().get(PdfName.SUBFILTER))
                && !PDFNAME_DOCTIMESTAMP.equals(obj.getDictionary().get(PdfName.SUBFILTER)))
                .collect(Collectors.toList());

        List<Signature> signInfos = new ArrayList<>();

        for (Presignature signature : signatures) {
            Certificate[] signCertificateChain = signature.getPkcs7().getSignCertificateChain();
            List<Certificate> certChain = (signCertificateChain != null && signCertificateChain.length > 0 && signCertificateChain[0] != null) ? Arrays.asList(signCertificateChain) : Collections.emptyList();

            for (Certificate cert : certChain) {
                Signature signInfo = new Signature((X509Certificate) cert, signature.getPkcs7().getSignDate().getTime());

                Field digestField = PdfPKCS7.class.getDeclaredField("digest");
                digestField.setAccessible(true);
                Object pkcs1Object = digestField.get(signature.getPkcs7());

                if (pkcs1Object instanceof byte[] bs) {
                    signInfo.setPkcs1(bs);
                }

                signInfos.add(signInfo);
            }

        }

        return signInfos.stream()
                .filter(obj -> obj.getSerialNumber() != null)
                .collect(Collectors.toList());
    }
    
}
