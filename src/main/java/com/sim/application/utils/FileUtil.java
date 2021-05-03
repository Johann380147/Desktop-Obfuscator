package com.sim.application.utils;

import com.sim.application.classes.JavaFile;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static ByteArrayOutputStream createZipByteArray(TreeItem<JavaFile> root) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (TreeItem<JavaFile> node : root.getChildren()) {
                zipFiles(node, zipOutputStream);
            }
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
    public static boolean saveToDisk(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (IOException e) {
            return false;
        }
        try (FileWriter myWriter = new FileWriter(filePath)) {
            myWriter.write(content);
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
