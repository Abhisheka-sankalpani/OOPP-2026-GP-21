package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Copies uploaded lecture notes into the project's materials folder using streams. */
public class FileStorage {

    private static final String ROOT = "materials";

    private FileStorage() { }

    /** Copies the file into materials/<courseId>/ and returns the relative path to store in the database. */
    public static String saveCopy(File source, String courseId) throws IOException {
        File folder = new File(ROOT, courseId);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not create the folder " + folder.getPath());
        }
        String name = System.currentTimeMillis() + "_" + source.getName();
        File target = new File(folder, name);

        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        return ROOT + "/" + courseId + "/" + name;
    }

    public static boolean delete(String relativePath) {
        File file = new File(relativePath);
        return file.exists() && file.delete();
    }

    public static File getFile(String relativePath) {
        return new File(relativePath);
    }

    /** File name without the folders and without the timestamp prefix. */
    public static String displayName(String relativePath) {
        String name = relativePath.substring(relativePath.lastIndexOf('/') + 1);
        int underscore = name.indexOf('_');
        return underscore >= 0 ? name.substring(underscore + 1) : name;
    }
}
