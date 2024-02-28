package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TorConfigToolApplication {

    public static void main(String[] args) {

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


        System.getProperties().put("server.port", 8080);

        SpringApplication.run(TorConfigToolApplication.class, args);

        // Call the Python script
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "data.py");
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            System.out.println("Python script output: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}