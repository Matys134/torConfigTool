package com.school.torconfigtool.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorrcConfigurator {

    // Create a logger (you can replace "TorrcConfigurator" with your actual class name)
    private static final Logger logger = Logger.getLogger(TorrcConfigurator.class.getName());

    public static void createTorrcFile(String filePath, String[] torrcLines) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                for (String line : torrcLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            // Log the exception with a meaningful message and level
            logger.log(Level.SEVERE, "Error creating Torrc file: " + filePath, e);
            // Optionally, you can rethrow the exception or handle it in another way
            // throw new YourCustomException("Error creating Torrc file", e);
        }
    }
}
