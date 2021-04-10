package com.sim.application.utils;

import com.sim.application.classes.JavaFile;
import javafx.scene.control.TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static ByteArrayOutputStream createZipByteArray(TreeItem<JavaFile> root) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
            for (TreeItem<JavaFile> node : root.getChildren()) {
                zipFiles(node, zipOutputStream);
            }
        } finally {
            zipOutputStream.close();
        }
        return byteArrayOutputStream;
    }
    private static void zipFiles(TreeItem<JavaFile> node, ZipOutputStream zipOutputStream) throws IOException {
        if (node != null && node.getValue() != null) {
            var file = node.getValue();
            if (node.getValue().isDirectory()) {
                for (TreeItem<JavaFile> child : node.getChildren()) {
                    zipFiles(child, zipOutputStream);
                }
            }
            else {
                ZipEntry zipEntry = new ZipEntry(file.getRelativePath());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(file.getObfuscatedContent().getBytes());
                zipOutputStream.closeEntry();
            }
        }
    }
    public static boolean saveToDisk(String location, ByteArrayOutputStream byteArrayOutputStream) {
        try(OutputStream outputStream = new FileOutputStream(location)) {
            byteArrayOutputStream.writeTo(outputStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileContent(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileExt(Path filePath) {
        return com.google.common.io.Files.getFileExtension(filePath.toAbsolutePath().toString());
    }
}
