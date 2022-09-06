package ec.gob.bomberosquito.firma_electronica_lib.utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfTemplate;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class PDFSigner {

    private static final PdfName PDFNAME_ETSI_RFC3161 = new PdfName("ETSI.RFC3161");
    private static final PdfName PDFNAME_DOCTIMESTAMP = new PdfName("DocTimeStamp");

    public static final String SIGNING_REASON = "signingReason";

    public static final String SIGNING_LOCATION = "signingLocation";

    public static final String SIGN_TIME = "signTime";

    public static final String SIGNATURE_PAGE = "signingPage";

    public static final String LAST_PAGE = "0";

    public static final String FONT_SIZE = "3";

    public static final String TYPE_SIG = "information1";

    public static final String INFO_QR = "";

    static {
        BouncyCastleUtils.initializeBouncyCastle();
    }

    public PDFSigner() {
    }

    public byte[] sign(byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties xParams) throws IOException, BadPasswordException {
        PdfStamper stp;
        Properties extraParams = (xParams != null) ? xParams : new Properties();
        
        String reason = extraParams.getProperty("signingReason");

        String location = extraParams.getProperty("signingLocation");

        String signTime = extraParams.getProperty("signTime");

        float fontSize = 3.0F;
        try {
            if (extraParams.getProperty("3") == null) {
                fontSize = 3.0F;
            } else {
                fontSize = Float.parseFloat(extraParams.getProperty("3").trim());
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.WARNING, "Se ha indicado un tama\ufffd\ufffdo de letra invalida (''{0}''), se usara el tama\ufffd\ufffdo por defecto: {1} {2}", new Object[]{extraParams.getProperty("3"), fontSize, e});
        }

        String typeSig = extraParams.getProperty("information1");
        if (typeSig == null) {
            typeSig = "information1";
        }

        if (typeSig.equals("QR") && extraParams.getProperty("3") == null) {
            fontSize = 4.5F;
        }

        String infoQR;
        if (extraParams.getProperty("") == null) {
            infoQR = "";
        } else {
            infoQR = extraParams.getProperty("").trim();
        }

        float fontLeading = fontSize;

        int page = 0;
        try {
            if (extraParams.getProperty("0") == null) {
                page = 0;
            } else {
                page = Integer.parseInt(extraParams.getProperty("0").trim());
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.WARNING, "Se ha indicado un numero de pagina invalido (''{0}''), se usara la ultima pagina: {1}", new Object[]{extraParams.getProperty("0"), e});
        }
        PdfReader pdfReader = new PdfReader(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            stp = PdfStamper.createSignature(pdfReader, baos, '\0', null, true);

            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            sap.setAcro6Layers(true);

            if (reason != null) {
                sap.setReason(reason);
            }

            if (location != null) {
                sap.setLocation(location);
            }

            if (signTime != null) {
                Date date = Utils.getSignTime(signTime);
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                sap.setSignDate(calendar);
            }

            if (page == 0 || page < 0 || page > pdfReader.getNumberOfPages()) {
                page = pdfReader.getNumberOfPages();
            }

            Rectangle signaturePositionOnPage = getSignaturePositionOnPage(extraParams);

            if (signaturePositionOnPage != null) {
                sap.setVisibleSignature(signaturePositionOnPage, page, null);
                X509Certificate x509Certificate = (X509Certificate) certChain[0];
                String informacionCertificado = x509Certificate.getSubjectDN().getName();
                String nombreFirmante = Utils.getCN(x509Certificate).toUpperCase();
                try {
                    ColumnText columnTextImage, columnText2;
                    PdfTemplate pdfTemplateImage;
                    Paragraph paragraph2;
                    String text;
                    ColumnText columnText;
                    Font font2;
                    Paragraph paragraph;
                    BufferedImage bufferedImage;
                    PdfTemplate pdfTemplate1, pdfTemplate2;
                    float y;
                    ColumnText columnText1;
                    float x, maxFontSize;
                    Paragraph paragraph1;
                    Font font1;
                    BaseFont baseFont;
                    Font font;
                    PdfTemplate pdfTemplate = sap.getLayer(0);
                    float width = pdfTemplate.getBoundingBox().getWidth();
                    float height = pdfTemplate.getBoundingBox().getHeight();
                    pdfTemplate.rectangle(0.0F, 0.0F, width, height);

                    switch (typeSig) {

                        case "QR":
                            pdfTemplate1 = sap.getLayer(2);
                            font = new Font(0, fontSize + fontSize / 2.0F, 1, Color.BLACK);
                            maxFontSize = getMaxFontSize(BaseFont.createFont(), nombreFirmante
                                    .trim(), width - width / 3.0F + 3.0F);
                            font.setSize(maxFontSize);
                            fontLeading = maxFontSize;

                            paragraph = new Paragraph("Firmado electrónicamente por:\n", new Font(0, fontSize / 1.25F, 0, Color.BLACK));

                            paragraph.add(new Paragraph(nombreFirmante.trim(), font));
                            paragraph.setAlignment(0);
                            paragraph.setLeading(fontLeading);
                            columnText = new ColumnText(pdfTemplate1);
                            columnText.setSimpleColumn(width / 3.0F + 3.0F, 0.0F, width, height);
                            columnText.addElement(paragraph);
                            columnText.go();

                            bufferedImage = null;

                            text = "FIRMADO POR: " + nombreFirmante.trim() + "\n";
                            text = text + "RAZON: " + reason + "\n";
                            text = text + "FECHA: " + signTime + "\n";
                            text = text + infoQR;
                            try {
                                bufferedImage = QRCode.generateQR(text, (int) height, (int) height);
                            } catch (Exception e) {
                            }

                            pdfTemplateImage = sap.getLayer(2);
                            columnTextImage = new ColumnText(pdfTemplateImage);
                            columnTextImage.setSimpleColumn(0.0F, 0.0F, width / 3.0F, height);
                            columnTextImage.setAlignment(1);
                            columnTextImage.addElement(Image.getInstance(bufferedImage, null));
                            columnTextImage.go();
                            break;

                        case "information1":
                            pdfTemplate1 = sap.getLayer(2);
                            font1 = new Font(2, fontSize + fontSize / 2.0F, 1, Color.BLACK);

                            paragraph1 = new Paragraph(nombreFirmante.trim(), font1);
                            paragraph1.setAlignment(2);
                            columnText1 = new ColumnText(pdfTemplate1);
                            columnText1.setSimpleColumn(0.0F, 0.0F, width / 2.0F - 1.0F, height);
                            columnText1.addElement(paragraph1);
                            columnText1.go();

                            pdfTemplate2 = sap.getLayer(2);
                            font2 = new Font(2, fontSize, 0, Color.DARK_GRAY);

                            paragraph2 = new Paragraph(fontLeading, "Nombre de reconocimiento " + informacionCertificado.trim() + "\nRaz��n: " + reason + "\nFecha: " + signTime, font2);
                            paragraph2.setAlignment(0);
                            columnText2 = new ColumnText(pdfTemplate2);
                            columnText2.setSimpleColumn(width / 2.0F + 1.0F, 0.0F, width, height);
                            columnText2.addElement(paragraph2);
                            columnText2.go();
                            break;

                        case "information2":
                            font = new Font(1, fontSize, 1, Color.BLACK);
                            baseFont = BaseFont.createFont();

                            x = Float.parseFloat(extraParams.getProperty("PositionOnPageLowerLeftX").trim());
                            y = Float.parseFloat(extraParams.getProperty("PositionOnPageLowerLeftY").trim());
                            nombreFirmante = nombreFirmante.replace(" ", "*");
                            width = baseFont.getWidthPoint(nombreFirmante, font.getSize());
                            nombreFirmante = nombreFirmante.replace("*", " ");
                            height = font.getSize() * 3.0F;
                            sap.setVisibleSignature(new Rectangle(x, y, x + width, y - height), page, null);
                            pdfTemplate = sap.getLayer(0);
                            pdfTemplate.rectangle(0.0F, 0.0F, width, height);
                            pdfTemplate1 = sap.getLayer(2);

                            paragraph = new Paragraph(fontLeading, "Firmado digitalmente por:\n", new Font(1, fontSize / 1.5F, 0, Color.BLACK));

                            paragraph.add(new Paragraph(fontLeading, nombreFirmante, font));
                            paragraph.add(new Paragraph(fontLeading, "Fecha: " + signTime, new Font(1, fontSize / 1.5F, 0, Color.BLACK)));

                            paragraph.setAlignment(0);
                            columnText = new ColumnText(pdfTemplate1);
                            columnText.setSimpleColumn(0.0F, 0.0F, width, height);
                            columnText.addElement(paragraph);
                            columnText.go();
                            break;
                    }

                } catch (DocumentException e) {
                    Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "Error al estampar la firma: {0}", e);
                }
            }
            
            sap.setCrypto(key, certChain, null, PdfSignatureAppearance.WINCER_SIGNED);

            try {
                stp.close();
            } catch (ExceptionConverter ec) {
                Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "Problemas con el driver: {0}", ec);
            } catch (DocumentException e) {
                Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "Error al estampar la firma: {0}", e);
            }
        } catch (DocumentException e) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "Error al crear la firma para estampar: {0}", e);
        }

        return baos.toByteArray();
    }

    private float getMaxFontSize(BaseFont baseFont, String text, float width) {
        float measureWidth = 1.0F;
        float fontSize = 0.1F;
        float oldSize = 0.1F;
        int repeat = 0;
        float multiply = 1.0F;
        text = text.replace(" ", "*");
        while (measureWidth < width) {
            repeat++;
            measureWidth = baseFont.getWidthPoint(text, fontSize);
            oldSize = fontSize;
            fontSize += 0.1F;
        }
        System.out.println("repeat: " + repeat);
        System.out.println("fontSize: " + fontSize);
        if (repeat > 60) {
            multiply = 1.0F;
        }
        if (repeat <= 60 && repeat > 20) {
            multiply = 2.0F;
        }
        if (repeat <= 20 && repeat > 10) {
            multiply = 3.0F;
        }
        if (repeat <= 10) {
            multiply = 4.0F;
        }

        if (fontSize > 20.0F) {
            oldSize = 20.0F;
            multiply = 1.0F;
        }
        return oldSize * multiply;
    }

    public List<SignInfo> getSigners(byte[] sign) throws IOException, Exception {
        AcroFields af = null;
        PdfReader pdfReader;
        if (!isPdfFile(sign)) {
            throw new Exception("El archivo no es un PDF");
        }

        try {
            pdfReader = new PdfReader(sign);
        } catch (IOException e) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "No se ha podido leer el PDF: {0}", af);
            throw new Exception("No se ha podido leer el PDF");
        }

        try {
            af = pdfReader.getAcroFields();
        } catch (Exception e) {
            Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "No se ha podido obtener la informacion de los firmantes del PDF, se devolvera un arbol vacio: {0}", e);

            throw new Exception("No se ha podido obtener la informacion de los firmantes del PDF", e);
        }

        List<String> names = af.getSignatureNames();

        Object pkcs1Object;
        List<SignInfo> signInfos = new ArrayList<>();

        for (String signatureName : names) {
            PdfPKCS7 pcks7;
            PdfDictionary pdfDictionary = af.getSignatureDictionary(signatureName);

            if (PDFNAME_ETSI_RFC3161.equals(pdfDictionary.get(PdfName.SUBFILTER)) || PDFNAME_DOCTIMESTAMP
                    .equals(pdfDictionary.get(PdfName.SUBFILTER))) {
                continue;
            }

            try {
                pcks7 = af.verifySignature(signatureName);
            } catch (Exception e) {
                Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "El PDF contiene una firma corrupta o con un formato desconocido ({0}), se continua con las siguientes si las hubiese: {1}", new Object[]{signatureName, e});
                continue;
            }

            Certificate[] signCertificateChain = pcks7.getSignCertificateChain();
            X509Certificate[] certChain = new X509Certificate[signCertificateChain.length];

            for (int i = 0; i < certChain.length; i++) {
                certChain[i] = (X509Certificate) signCertificateChain[i];
            }

            SignInfo signInfo = new SignInfo(certChain, pcks7.getSignDate().getTime());

            try {
                Field digestField;
                digestField = Class.forName("com.lowagie.text.pdf.PdfPKCS7").getDeclaredField("digest");
                digestField.setAccessible(true);
                pkcs1Object = digestField.get(pcks7);

                if (pkcs1Object instanceof byte[]) {
                    signInfo.setPkcs1((byte[]) pkcs1Object);
                }
            } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, "No se ha podido obtener informacion de una de las firmas del PDF, se continuara con la siguiente: {0}", ex);
                continue;
            }

            signInfos.add(signInfo);
        }

        return signInfos;
    }

    private boolean isPdfFile(byte[] data) {
        byte[] buffer = new byte["%PDF-".length()];

        try {
            (new ByteArrayInputStream(data)).read(buffer);
        } catch (IOException e) {
            buffer = null;
        }

        return !(buffer != null && !"%PDF-".equals(new String(buffer)));
    }

    private static Rectangle getSignaturePositionOnPage(Properties extraParams) {
        return PdfUtil.getPositionOnPage(extraParams);
    }
}
