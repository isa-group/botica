package com.botica.utils.directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryOperations {

    private DirectoryOperations(){
    }

    public static void createDir(Path filePath) {

        Path parent = filePath.getParent();

        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getProjectName(){
        String baseDir = System.getProperty("user.dir");
        File file = new File(baseDir);
        return file.getName().toLowerCase();
    }
}
