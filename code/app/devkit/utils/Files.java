package devkit.utils;

import play.Application;
import play.Play;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
/**
 * Files utils
 */
public class Files {

    public static File applicationRoot(){
        String path = Play.application().path().getAbsolutePath();
        if(path.endsWith("/./target/..")){
            path = path.substring(0, path.length() - "/./target/..".length());
        }else if(path.endsWith("/target/./..")){
            path = path.substring(0, path.length() - "/target/./..".length());
        }

        File dir = new File(path);

        if(dir.exists() && dir.isDirectory()){
            return dir;
        }
        return Play.application().path();
    }

    /**
     * Just copy a file
     * @param from
     * @param to
     */
    public static void copy(File from, File to) {
        if (from.getAbsolutePath().equals(to.getAbsolutePath())) {
            return;
        }
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(from);
            os = new FileOutputStream(to);
            int read;
            byte[] buffer = new byte[10000];
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
            try {
                os.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Just delete a file. If the file is a directory, it's work.
     * @param file The file to delete
     */
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return file.delete();
        }
    }

    /**
     * Recursively delete a directory.
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file: files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    public static boolean copyDir(File from, File to) {
        try {
            Files.copyDirectory(from, to);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void copyDirectory(File srcDir, File destDir) throws IOException {
       if (destDir.exists()) {
           if (destDir.isDirectory() == false) {
               throw new IOException("Destination '" + destDir + "' exists but is not a directory");
           }
       } else {
           if (destDir.mkdirs() == false) {
               throw new IOException("Destination '" + destDir + "' directory cannot be created");
           }
           destDir.setLastModified(srcDir.lastModified());
       }

       if (destDir.canWrite() == false) {
           throw new IOException("Destination '" + destDir + "' cannot be written to");
       }
       // recurse
       File[] files = srcDir.listFiles();
       if (files == null) {  // null if security restricted
               throw new IOException("Failed to list contents of " + srcDir);
           }
       for (int i = 0; i < files.length; i++) {
               File copiedFile = new File(destDir, files[i].getName());
               if (files[i].isDirectory()) {
                   copyDirectory(files[i], copiedFile);
                   } else {
                       copy(files[i], copiedFile);
                   }
           }
       }

    public static void unzip(File from, File to) {
        try {
            String outDir = to.getCanonicalPath();
            ZipFile zipFile = new ZipFile(from);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    new File(to, entry.getName()).mkdir();
                    continue;
                }
                File f = new File(to, entry.getName());
                if(!f.getCanonicalPath().startsWith(outDir)) {
                    throw new IOException("Corrupted zip file");
                }
                f.getParentFile().mkdirs();
                FileOutputStream os = new FileOutputStream(f);
                Files.copy(zipFile.getInputStream(entry), os);
                os.close();
            }
            zipFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy an stream to another one.
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        try {
            int read = 0;
            byte[] buffer = new byte[8096];
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch(IOException e) {
            throw e;
        } finally {
            try {
                is.close();
            } catch(Exception e) {
                //
            }
        }
    }


    public static void zip(File directory, File zipFile) throws Exception {
        FileOutputStream os = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(os);
        zipDirectory(directory, directory, zos);
        zos.close();
        os.close();
    }

    static void zipDirectory(File root, File directory, ZipOutputStream zos) throws Exception {
        for (File item : directory.listFiles()) {
            if (item.isDirectory()) {
                zipDirectory(root, item, zos);
            } else {
                byte[] readBuffer = new byte[2156];
                int bytesIn;
                FileInputStream fis = new FileInputStream(item);
                String path = item.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                ZipEntry anEntry = new ZipEntry(path);
                zos.putNextEntry(anEntry);
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                fis.close();
            }
        }
    }
}
