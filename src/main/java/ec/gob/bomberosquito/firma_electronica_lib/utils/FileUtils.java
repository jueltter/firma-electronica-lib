package ec.gob.bomberosquito.firma_electronica_lib.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Luis Fernando Ordóñez Armijos
 * @version 20 de Agosto de 2019
 */
public class FileUtils {

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] fileConvertToByteArray(File file) throws IOException {
        Path documentoPath = Paths.get(file.getAbsolutePath(), new String[0]);
        return Files.readAllBytes(documentoPath);
    }

    public static void saveByteArrayToDisc(byte[] archivo, String rutaNombre) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(rutaNombre);
        File arc = new File(rutaNombre);

        Long espacio = arc.getFreeSpace();

        if (archivo != null) {
            System.out.println("bytes: " + archivo.length + " espacio " + espacio);

            if (espacio < archivo.length) {
                throw new IOException("No se puede crear el archivo firmado. No hay espacio suficiente en el disco");
            }
            fos.write(archivo);
            fos.close();
        } else {
            throw new IOException("No se puede firmar un documento de 0 bytes");
        }
    }

    public static String crearNombreFirmado(File documento, String extension) throws IOException {
        String nombre = crearNombre(documento);
        if ((new File(nombre + "-signed" + extension)).exists()) {
            nombre = nombre + "-new";
        }
        return nombre + "-signed" + extension;
    }

    public static String crearNombreVerificado(File documento, String extension) throws IOException {
        String hora = TiempoUtils.getFechaHoraServidor().replace(":", "").replace(" ", "").replace(".", "").replace("-", "").substring(0, 20);
        String nombre = crearNombre(documento);
        if (extension.isEmpty()) {
            extension = getExtension(nombre);
        }
        return nombre + "-verified-" + hora + extension;
    }

    private static String crearNombre(File documento) {
        String nombreCompleto = documento.getAbsolutePath();
        return nombreCompleto.replaceFirst("[.][^.]+$", "");
    }

    public static String getExtension(String fileName) {
        String extension = "";
        if (fileName.contains(".")) {
            extension = "." + fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return extension;
    }

    public static void abrirDocumento(String documento) throws IOException {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            String cmd = "rundll32 url.dll,FileProtocolHandler " + documento;
            Runtime.getRuntime().exec(cmd);
        } else {
            File doc = new File(documento);
            Desktop.getDesktop().open(doc);
        }
    }

    public static String rutaFichero(FileNameExtensionFilter filtro) {
        String ruta = "";
        JFileChooser jFileChooser = new JFileChooser(new File(System.getProperty("user.home")));
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setEnabled(false);
        jFileChooser.setFileFilter(filtro);

        int resultado = jFileChooser.showOpenDialog(null);
        if (resultado == 0) {
            File file = new File(jFileChooser.getSelectedFile().toString());
            if (file.exists() && file.isFile()) {
                for (String filtre : filtro.getExtensions()) {
                    if (getFileExtension(file).equals(filtre)) {
                        ruta = file.toString();
                    }
                }
            }
        }
        return ruta;
    }
}
