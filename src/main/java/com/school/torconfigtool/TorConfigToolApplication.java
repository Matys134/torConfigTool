package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TorConfigToolApplication {

    public static void main(String[] args) throws IOException {

        Path torrcPath = Paths.get("torrc", "dataDirectory");
        if (!Files.exists(torrcPath)) {
            try {
                // Create the directory
                Files.createDirectories(torrcPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // create the directory for onion
        Path onionPath = Paths.get("onion", "hiddenServiceDirs");
        if (!Files.exists(onionPath)) {
            try {
                // Create the directory
                Files.createDirectories(onionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //ProcessBuilder processBuilder = new ProcessBuilder("python3", "src/main/java/com/school/torconfigtool/python/data.py");
        //processBuilder.start();

        System.getProperties().put("server.port", 8443);

        SpringApplication app = new SpringApplication(TorConfigToolApplication.class);
        app.run(args);
    }
}