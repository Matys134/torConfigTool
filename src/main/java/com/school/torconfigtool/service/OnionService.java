package com.school.torconfigtool.service;

import com.school.torconfigtool.util.RelayUtils;
import com.school.torconfigtool.model.TorConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.school.torconfigtool.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

/**
 * Service for handling Onion Service related operations.
 */
@Service
public class OnionService {
    private static final Logger logger = LoggerFactory.getLogger(OnionService.class);
    private final NginxService nginxService;
    private final TorConfig torConfig;

    public OnionService(NginxService nginxService, TorConfig torConfig) {
        this.nginxService = nginxService;
        this.torConfig = torConfig;
    }

    /**
     * Retrieves all onion service ports.
     * @return List of all onion service ports.
     */
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

    /**
     * Starts the Tor Onion Service.
     * @return true if the service started successfully, false otherwise.
     */
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

    /**
     * Creates a Torrc file for the given onion service port.
     * @param filePath The path to the file.
     * @param onionServicePort The onion service port.
     * @throws IOException If an I/O error occurs.
     */
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
            torrcWriter.newLine();
            // Write the DataDirectory configuration to the file
            String dataDirectoryPath = currentDirectory + "/" + TORRC_DIRECTORY_PATH + "dataDirectory/onion_" + onionServicePort;
            torrcWriter.write("DataDirectory " + dataDirectoryPath);
            torrcWriter.newLine();

            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
    }

    /**
     * Reads the hostname file for the given port.
     * @param port The port.
     * @return The hostname.
     */
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

    /**
     * Configures the onion service for the given port.
     * @param onionServicePort The onion service port.
     * @throws IOException If an I/O error occurs.
     */
    public void configureOnionService(int onionServicePort) throws IOException {
        // Check port availability before configuring the onion service
        if (!RelayUtils.isPortAvailable(TORRC_FILE_PREFIX + onionServicePort + "_onion", onionServicePort)) {
            throw new IOException("Port is not available.");
        }

        String pathToFile = TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + onionServicePort + "_onion";
        if (!new File(pathToFile).exists()) {
            createTorrcFile(pathToFile, onionServicePort);
            nginxService.generateNginxConfig(onionServicePort);

            // Restart nginx
            nginxService.reloadNginx();
        }
        torConfig.setHiddenServicePort(String.valueOf(onionServicePort));
        logger.info("Hidden Service Port set to: {}", onionServicePort);
    }

    /**
     * Refreshes Nginx.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted.
     */
    public void refreshNginx() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "reload", "nginx");
        Process process = processBuilder.start();
        process.waitFor();
    }

    /**
     * Checks if the Onion service is configured.
     * @return true if the Onion service is configured, false otherwise.
     */
    public boolean checkOnionConfigured() {
        return !getAllOnionServicePorts().isEmpty();
    }

    /**
     * Retrieves the current hostnames.
     * @return a map of the current hostnames.
     */
    public Map<String, String> getCurrentHostnames() {
        Map<String, String> hostnames = new HashMap<>();
        for (String hiddenServicePortString : getAllOnionServicePorts()) {
            if (hiddenServicePortString != null) {
                String hostname = readHostnameFile(Integer.parseInt(hiddenServicePortString));
                hostnames.put(hiddenServicePortString, hostname);
            }
        }
        return hostnames;
    }
}