package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
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

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service for handling Onion Service related operations.
 */
@Service
public class OnionService {
    private final NginxService nginxService;
    private final TorConfig torConfig;
    private final CommandService commandService;
    private final TorrcWriteConfigService torrcWriteConfigService;

    public OnionService(NginxService nginxService, TorConfig torConfig, CommandService commandService, TorrcWriteConfigService torrcWriteConfigService) {
        this.nginxService = nginxService;
        this.torConfig = torConfig;
        this.commandService = commandService;
        this.torrcWriteConfigService = torrcWriteConfigService;
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
            // Handle any exceptions that occur during the start
            return false;
        }
    }

    public void setupOnionService(String filePath, int onionServicePort, String nickname) throws IOException {
        File torrcFile = new File(filePath);

        // Check if the parent directories exist; if not, attempt to create them
        if (!torrcFile.getParentFile().exists() && !torrcFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directories for: " + filePath);
        }

        try (BufferedWriter torrcWriter = new BufferedWriter(new FileWriter(torrcFile))) {
            torrcWriteConfigService.writeOnionServiceConfig(onionServicePort, nickname, torrcWriter);
            nginxService.createIndexFile(onionServicePort, System.getProperty("user.dir"));
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
        Path path = Paths.get(currentDirectory, "onion", "hiddenServiceDirs", "onion-service-"
                + port, "hostname");
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
    public void configureOnionService(int onionServicePort, String nickname) throws IOException {
        // Check port availability before configuring the onion service
        if (!RelayUtilityService.portsAreAvailable(TORRC_FILE_PREFIX + nickname + "_onion",
                onionServicePort)) {
            throw new IOException("Port is not available.");
        }

        String pathToFile = TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + nickname + "_onion";
        if (!new File(pathToFile).exists()) {
            setupOnionService(pathToFile, onionServicePort, nickname);
            nginxService.configureNginxForOnionService(onionServicePort);

            // Restart nginx
            nginxService.reloadNginx();
        }
        torConfig.getOnionConfig().setHiddenServicePort(String.valueOf(onionServicePort));
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

    /**
     * Removes the Nginx configuration and symbolic link files associated with the given relay nickname.
     * This method is used when you want to completely remove an onion service from the system.
     *
     * @param relayNickname The nickname of the relay for which the onion files should be removed.
     * @throws IOException If an I/O error occurs during the execution of the remove commands.
     * @throws InterruptedException If the current thread is interrupted while waiting for the command execution process to complete.
     */
    public void removeOnionFiles(String relayNickname) throws IOException, InterruptedException {
        String removeNginxConfigCommand = "sudo rm -f /etc/nginx/sites-available/onion-service-" + relayNickname;
        String removeSymbolicLinkCommand = "sudo rm -f /etc/nginx/sites-enabled/onion-service-" + relayNickname;

        commandService.executeCommand(removeNginxConfigCommand);
        commandService.executeCommand(removeSymbolicLinkCommand);
    }
}