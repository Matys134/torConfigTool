package com.school.torconfigtool.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TorrcConfigurator {

    public static void createTorrcFile(String filePath, String[] torrcLines) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : torrcLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
