package com.sim.application.utils;

import com.sim.application.classes.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static byte[] createZipByteArray(List<File> files) throws IOException {
        Set<String> addedNames = new HashSet<String>();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
            for (File file : files) {
                if (addedNames.contains(file.getFileName()))
                    continue;

                ZipEntry zipEntry = new ZipEntry(file.getFileName());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(file.getContent());
                zipOutputStream.closeEntry();
                addedNames.add(file.getFileName());
            }
        } finally {
            zipOutputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] getFileContent(Path filePath) {
        try {
            return Files.readString(filePath).getBytes();
        }
        catch (IOException e) {
            return null;
        }
    }

    public static String getFileExt(Path filePath) {
        return com.google.common.io.Files.getFileExtension(filePath.toAbsolutePath().toString());
    }
}
