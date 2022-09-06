package ec.gob.bomberosquito.firma_electronica_lib.utils;

import com.lowagie.text.Rectangle;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class PdfUtil {

    public static Rectangle getPositionOnPage(Properties extraParams) {
        if (extraParams == null) {
            Logger.getLogger(PdfUtil.class.getName()).severe("Se ha pedido una posicion para un elemento grafico nulo");
            return null;
        }

        if (extraParams.getProperty("PositionOnPageLowerLeftX") != null && extraParams
                .getProperty("PositionOnPageLowerLeftY") != null && extraParams
                .getProperty("PositionOnPageUpperRightX") != null && extraParams
                .getProperty("PositionOnPageUpperRightY") != null) {
            try {
                return new Rectangle(Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftX").trim()),
                        Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftY").trim()),
                        Integer.parseInt(extraParams.getProperty("PositionOnPageUpperRightX").trim()),
                        Integer.parseInt(extraParams.getProperty("PositionOnPageUpperRightY").trim()));
            } catch (NumberFormatException e) {
                Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, "Se ha indicado una posicion invalida para la firma: {0}", e);
            }
        }

        if (extraParams.getProperty("PositionOnPageLowerLeftX") != null && extraParams
                .getProperty("PositionOnPageLowerLeftY") != null && extraParams
                .getProperty("PositionOnPageUpperRightX") == null && extraParams
                .getProperty("PositionOnPageUpperRightY") == null) {
            try {
                return new Rectangle(Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftX").trim()),
                        Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftY").trim()), (Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftX").trim()) + 110), (Integer.parseInt(extraParams.getProperty("PositionOnPageLowerLeftY").trim()) - 36));
            } catch (NumberFormatException e) {
                Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, "Se ha indicado una posicion invalida para la firma: {0}", e);
            }
        }

        return null;
    }
}
