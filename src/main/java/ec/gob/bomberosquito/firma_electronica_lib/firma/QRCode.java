package ec.gob.bomberosquito.firma_electronica_lib.firma;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class QRCode {

    public static void main(String[] args) {
        try {
            File file = new File("qrCode.png");
            String text = "Nombre firmante: MISAEL VLADIMIR FERNANDEZ CORREA\nRazón: Firmado digitalmente con RUBRICA\nFecha firmado: 2018-05-31T11:39:47.247-05:00\nFirmado digitalmente con RUBRICA\nhttps://minka.gob.ec/rubrica/rubrica";

            BufferedImage bufferedImage = QRCode.generateQR(text, 300, 300);
            ImageIO.write(bufferedImage, "png", file);
            System.out.println("QRCode Generated: " + file.getAbsolutePath());

            String qrString = QRCode.decoder(file);
            System.out.println("Text QRCode: " + qrString);
        } catch (Exception ex) {
            Logger.getLogger(QRCode.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static BufferedImage generateQR(String text, int h, int w) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);

        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.US_ASCII.name());

        hints.put(EncodeHintType.MARGIN, 0);

        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, w, h, hints);

        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), 1);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrix.getWidth(), matrix.getHeight());
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrix.getWidth(); i++) {
            for (int j = 0; j < matrix.getHeight(); j++) {
                if (matrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        return image;
    }

    public static String decoder(File file) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);

        BufferedImage image = ImageIO.read(inputStream);

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(bufferedImageLuminanceSource));

        QRCodeReader reader = new QRCodeReader();
        Result result = reader.decode(bitmap);
        return result.getText();
    }
}
