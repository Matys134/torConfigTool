package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class OnionService {
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    private static final Logger logger = LoggerFactory.getLogger(OnionService.class);

    public List<String> getAllOnionServicePorts() {
        List<String> ports = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] torrcFiles = torrcDirectory.listFiles((dir, name) -> name.endsWith("_onion"));

        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                String fileName = file.getName();
                String port = fileName.substring(fileName.indexOf('-') + 1, fileName.indexOf('_'));
                ports.add(port);
            }
        }

        return ports;
    }

    public boolean startTorOnion() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "tor");
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Tor Onion Service", e);
            // Log and handle any exceptions that occur during the start
            return false;
        }
    }

    public void createTorrcFile(String filePath, int onionServicePort) throws IOException {
        File torrcFile = new File(filePath);

        // Check if the parent directories exist; if not, attempt to create them
        if (!torrcFile.getParentFile().exists() && !torrcFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directories for: " + filePath);
        }

        try (BufferedWriter torrcWriter = new BufferedWriter(new FileWriter(torrcFile))) {
            String currentDirectory = System.getProperty("user.dir");
            String hiddenServiceDirs = currentDirectory + "/onion/hiddenServiceDirs";

            File wwwDir = new File(currentDirectory + "/onion/www");
            if (!wwwDir.exists() && !wwwDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
            }

            File serviceDir = new File(wwwDir, "service-" + onionServicePort);
            if (!serviceDir.exists() && !serviceDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
            }

            torrcWriter.write("HiddenServiceDir " + hiddenServiceDirs + "/onion-service-" + onionServicePort + "/");
            torrcWriter.newLine();
            torrcWriter.write("HiddenServicePort 80 127.0.0.1:" + onionServicePort);
            torrcWriter.newLine();
            torrcWriter.write("RunAsDaemon 1");
            torrcWriter.newLine();
            torrcWriter.write("SocksPort 0");

            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
    }

    public String readHostnameFile(int port) {
        // Get the current working directory
        String currentDirectory = System.getProperty("user.dir");

        // Build the correct path to the hostname file
        Path path = Paths.get(currentDirectory, "onion", "hiddenServiceDirs", "onion-service-" + port, "hostname");
        System.out.println(path);
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "Unable to read hostname file";
        }
    }
}
