package ec.gob.bomberosquito.firma_electronica_lib.firma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class TiempoUtils {

    private static final Logger LOGGER = Logger.getLogger(TiempoUtils.class.getName());

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static Date getFechaHora() {
        String fechaHora = null;
        try {
            fechaHora = getFechaHoraServidor();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se puede obtener la fecha del servidor: {0}", e.getMessage());
        }

        try {
            TemporalAccessor accessor = DATE_TIME_FORMATTER.parse(fechaHora);
            return Date.from(Instant.from(accessor));
        } catch (DateTimeParseException e) {
            LOGGER.log(Level.SEVERE, "La fecha indicada (''{0}'') no sigue el patron ISO-8601: {1}", new Object[]{fechaHora, e});
            return new Date();
        }
    }

    public static String getFechaHoraServidor() throws IOException {
        /*String fecha_hora_url = "https://api.firmadigital.gob.ec/api/fecha-hora";
        System.out.println("fecha_hora_url: " + fecha_hora_url);
        if (fecha_hora_url.isEmpty()) {
            return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        URL url = new URL(fecha_hora_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(2000);
        int responseCode = con.getResponseCode();
        LOGGER.log(Level.FINE, "GET Response Code: {0}", responseCode);
        System.out.println("GET Response Code: " + responseCode);

        if (responseCode == 200) {
            try (InputStream is = con.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(reader);

                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                return response.toString();
            }
        }*/
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
