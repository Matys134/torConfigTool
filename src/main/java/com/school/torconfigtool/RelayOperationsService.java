package com.school.torconfigtool;

import com.school.torconfigtool.exception.RelayOperationException;
import com.school.torconfigtool.service.NginxService;
import com.simtechdata.waifupnp.UPnP;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a Service class for managing processes.
 */
@Service
public class RelayOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsService.class);
    private final TorConfigurationService torConfigurationService;
    private final RelayOperationsService relayOperationsService;
    private final NginxService nginxService;
    private final OnionRelayOperationsService onionRelayOperationsService;
    private final TorrcService torrcService;
    private final RelayStatusService relayStatusService;

    public RelayOperationsService(TorConfigurationService torConfigurationService, RelayOperationsService relayOperationsService, NginxService nginxService, OnionRelayOperationsService onionRelayOperationsService, TorrcService torrcService, RelayStatusService relayStatusService) {
        this.torConfigurationService = torConfigurationService;
        this.relayOperationsService = relayOperationsService;
        this.nginxService = nginxService;
        this.onionRelayOperationsService = onionRelayOperationsService;
        this.torrcService = torrcService;
        this.relayStatusService = relayStatusService;
    }

    /**
     * This method executes a bash command and returns its exit code.
     *
     * @param command The bash command to execute.
     * @return int The exit code of the command.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to finish.
     */
    public int executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        // Log the command being executed
        logger.info("Executing command: {}", command);

        try {
            int exitCode = process.waitFor();

            // Log the exit code
            logger.info("Command exit code: {}", exitCode);

            return exitCode;
        } finally {
            process.destroy();
        }
    }


    public List<Integer> getUPnPPorts() {
        List<Integer> upnpPorts = new ArrayList<>();
        List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "guard");
        for (TorConfiguration config : guardConfigs) {
            int orPort = getOrPort(torrcService.buildTorrcFilePath(config.getGuardConfig().getNickname(), "guard"));
            if (UPnP.isMappedTCP(orPort)) {
                upnpPorts.add(orPort);
            }
        }
        return upnpPorts;
    }

    public void waitForStatusChange(String relayNickname, String relayType, String expectedStatus) throws InterruptedException {
        System.out.println("Waiting for status change");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds timeout
            String status = relayStatusService.getRelayStatus(relayNickname, relayType);
            System.out.println("Status: " + status);
            if (expectedStatus.equals(status)) {
                checkAndManageNginxStatus();
                break;
            }
            Thread.sleep(500); // wait for 500 milliseconds before the next check
        }
    }

    public void checkAndManageNginxStatus() {
        // Get the list of all webTunnels and Onion services
        List<String> allServices = nginxService.getAllServices();

        // Iterate over the list and check the status of each service
        for (String service : allServices) {
            String status = relayStatusService.getRelayStatus(service, "onion");
            // If at least one service is online, start the Nginx service and return
            if ("online".equals(status)) {
                nginxService.startNginx();
                return;
            }
        }

        // If no service is online, stop the Nginx service
        nginxService.stopNginx();
    }

    public String changeRelayStateWithoutFingerprint(String relayNickname, String relayType, Model model) {
        Path torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType);
        String operation = "start";
        try {
            processRelayOperationWithoutFingerprint(torrcFilePath, relayNickname);
            model.addAttribute("successMessage", "Tor Relay " + operation + "ed successfully!");
        } catch (RelayOperationException | IOException | InterruptedException e) {
            logger.error("Failed to {} Tor Relay for relayNickname: {}", operation, relayNickname, e);
            model.addAttribute("errorMessage", "Failed to " + operation + " Tor Relay.");
        }
        return relayOperations(model);
    }

    private void processRelayOperationWithoutFingerprint(Path torrcFilePath, String relayNickname) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RelayOperationException("Torrc file does not exist for relay: " + relayNickname);
        }
        {
            // Step 3: Start the Relay
            String command = "tor -f " + torrcFilePath.toAbsolutePath();
            System.out.println("Executing command: " + command);
            int exitCode = relayOperationsService.executeCommand(command);
            if (exitCode != 0) {
                throw new RelayOperationException("Failed to start Tor Relay service.");
            }
        }
    }

    public String changeRelayState(String relayNickname, String relayType, Model model, boolean start) {
        Path torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType);
        String operation = start ? "start" : "stop";
        try {
            processRelayOperation(torrcFilePath, relayNickname, start);
            model.addAttribute("successMessage", "Tor Relay " + operation + "ed successfully!");
        } catch (RelayOperationException | IOException | InterruptedException e) {
            logger.error("Failed to {} Tor Relay for relayNickname: {}", operation, relayNickname, e);
            model.addAttribute("errorMessage", "Failed to " + operation + " Tor Relay.");
        }
        return relayOperations(model);
    }

    private void processRelayOperation(Path torrcFilePath, String relayNickname, boolean start) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RelayOperationException("Torrc file does not exist for relay: " + relayNickname);
        }
        if (start) {
            // Step 1: Retrieve Fingerprints
            List<String> allFingerprints = getAllRelayFingerprints();

            // Step 2: Update the torrc File with fingerprints
            updateTorrcWithFingerprints(torrcFilePath, allFingerprints);

            // Step 3: Start the Relay
            String command = "tor -f " + torrcFilePath.toAbsolutePath();
            System.out.println("Executing command: " + command);
            int exitCode = relayOperationsService.executeCommand(command);
            if (exitCode != 0) {
                throw new RelayOperationException("Failed to start Tor Relay service.");
            }


        } else {
            int pid = relayStatusService.getTorRelayPID(torrcFilePath.toString());
            if (pid > 0) {
                String command = "kill -SIGINT " + pid;
                int exitCode = relayOperationsService.executeCommand(command);
                if (exitCode != 0) {
                    throw new RelayOperationException("Failed to stop Tor Relay service.");
                }
            } else if (pid == -1) {
                throw new RelayOperationException("Tor Relay is not running.");
            } else {
                throw new RelayOperationException("Error occurred while retrieving PID for Tor Relay.");
            }
        }
    }

    // This method could be moved from RelayController to here
    private List<String> getFingerprints(String dataDirectoryPath) {
        // Assuming the dataDirectoryPath is something like "torrc/dataDirectory"
        List<String> fingerprints = new ArrayList<>();
        File dataDirectory = new File(dataDirectoryPath);
        File[] dataDirectoryFiles = dataDirectory.listFiles(File::isDirectory);

        if (dataDirectoryFiles != null) {
            for (File dataDir : dataDirectoryFiles) {
                String fingerprintFilePath = dataDir.getAbsolutePath() + File.separator + "fingerprint";
                String fingerprint = readFingerprint(fingerprintFilePath);
                if (fingerprint != null) {
                    fingerprints.add(fingerprint);
                }
            }
        }
        return fingerprints;
    }

    private String readFingerprint(String fingerprintFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fingerprintFilePath))) {
            return reader.readLine().split(" ")[1].trim();
        } catch (IOException e) {
            logger.error("Failed to read fingerprint file: {}", fingerprintFilePath, e);
            return null;
        }
    }

    // This new method would retrieve fingerprints from all existing relays
    private List<String> getAllRelayFingerprints() {
        // This path should lead to the base directory where all relay data directories are stored
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        return getFingerprints(dataDirectoryPath);
    }

    // This new method would update or append the fingerprints to the torrc configuration file
    // This new method would update the MyFamily line with current fingerprints
    private void updateTorrcWithFingerprints(Path torrcFilePath, List<String> currentFingerprints) throws IOException {
        // Read the existing torrc file content
        List<String> fileContent = new ArrayList<>();
        boolean myFamilyUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // If the MyFamily line is encountered, update it with the current fingerprints
                if (line.startsWith("MyFamily")) {
                    if (!currentFingerprints.isEmpty()) {
                        line = "MyFamily " + String.join(", ", currentFingerprints);
                        myFamilyUpdated = true;
                    } else {
                        // If there are no current fingerprints, remove the MyFamily line
                        continue;
                    }
                }
                fileContent.add(line);
            }
        }

        // If MyFamily line was not in the file and we have fingerprints, add it
        if (!myFamilyUpdated && !currentFingerprints.isEmpty()) {
            fileContent.add("MyFamily " + String.join(", ", currentFingerprints));
        }

        // Write the updated content back to the torrc file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(torrcFilePath.toFile()))) {
            for (String line : fileContent) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public String buildDataDirectoryPath(String relayNickname) {
        return System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + relayNickname;
    }

    // New method to get the webtunnel link
    public String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = buildDataDirectoryPath(relayNickname);
        String fingerprintFilePath = dataDirectoryPath + File.separator + "fingerprint";
        String fingerprint = readFingerprint(fingerprintFilePath);

        // Construct the path to the torrc file
        String torrcFilePath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "torrc-" + relayNickname + "_bridge";

        String webtunnelDomainAndPath = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line starts with "ServerTransportOptions webtunnel url"
                if (line.startsWith("ServerTransportOptions webtunnel url")) {
                    // Extract the webtunnel domain and path from the line
                    webtunnelDomainAndPath = line.split("=")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read torrc file: {}", torrcFilePath, e);
        }

        // Replace the "https://yourdomain/path" in the webtunnel link with the extracted webtunnel domain and path

        return "webtunnel 10.0.0.2:443 " + fingerprint + " url=" + webtunnelDomainAndPath;
    }

    public int getOrPort(Path torrcFilePath) {
        int orPort = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))){
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ORPort")) {
                    orPort = Integer.parseInt(line.split(" ")[1]);
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read ORPort from torrc file: {}", torrcFilePath, e);
        }
        return orPort;
    }

    public void closeOrPort(String relayNickname, String relayType) {
        Path torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType);
        int orPort = getOrPort(torrcFilePath);
        UPnP.closePortTCP(orPort);
    }

    public String relayOperations(Model model) {
        System.out.println("Inside relayOperations method");
        String folderPath = torConfigurationService.buildFolderPath();
        model.addAttribute("guardConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "guard"));

        model.addAttribute("bridgeConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "bridge"));

        model.addAttribute("onionConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "onion"));
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurationsFromFolder(folderPath, "onion");

        logger.info("OnionConfigs: {}", onionConfigs);

        // Create a map to store hostnames for onion services
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfiguration config : onionConfigs) {
            String hostname = onionRelayOperationsService.readHostnameFile(config.getHiddenServicePort());
            hostnames.put(config.getHiddenServicePort(), hostname);
            logger.info("Hostname for port {}: {}", config.getHiddenServicePort(), hostname);
        }

        List<TorConfiguration> bridgeConfigs = torConfigurationService.readTorConfigurationsFromFolder(folderPath, "bridge");
        Map<String, String> webtunnelLinks = new HashMap<>();
        for (TorConfiguration config : bridgeConfigs) {
            String webtunnelLink = getWebtunnelLink(config.getBridgeConfig().getNickname());
            webtunnelLinks.put(config.getBridgeConfig().getNickname(), webtunnelLink);
            logger.info("Added webtunnel link for " + config.getBridgeConfig().getNickname() + ": " + webtunnelLink);
        }
        model.addAttribute("webtunnelLinks", webtunnelLinks);

        logger.info("Hostnames: {}", hostnames);
        model.addAttribute("hostnames", hostnames);

        List<Integer> upnpPorts = getUPnPPorts();
        model.addAttribute("upnpPorts", upnpPorts);

        return "relay-operations";
    }

    public String stopRelay(String relayNickname, String relayType, Model model) {
        String view = changeRelayState(relayNickname, relayType, model, false);

        new Thread(() -> {
            try {
                waitForStatusChange(relayNickname, relayType, "offline");
                // Close the ORPort after the relay has stopped
                closeOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to stop", e);
            }
        }).start();

        return view;
    }

    public String startRelay(String relayNickname, String relayType, Model model) {
        String view;
        if ("guard".equals(relayType)) {
            view = changeRelayState(relayNickname, relayType, model, true);
        } else {
            view = changeRelayStateWithoutFingerprint(relayNickname, relayType, model);
        }
        System.out.println("Relay state changed");

        new Thread(() -> {
            try {
                waitForStatusChange(relayNickname, relayType, "online");
                openOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to start", e);
            }
        }).start();
        System.out.println("Returning view");

        return view;
    }

    public Map<String, Object> removeRelay(String relayNickname, String relayType) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Build paths for Torrc file and DataDirectory
            Path torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType);
            String dataDirectoryPath = buildDataDirectoryPath(relayNickname);

            // Delete Torrc file
            Files.deleteIfExists(torrcFilePath);

            // Delete DataDirectory
            FileUtils.deleteDirectory(new File(dataDirectoryPath));

            // Build paths for Onion files in /onion folder and its corresponding file in torrc directory
            Path onionFilePath = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs", "onion-service-" + relayNickname);
            Path torrcOnionFilePath = Paths.get(System.getProperty("user.dir"), "torrc", "torrc-" + relayNickname + "_onion");

            // Delete Onion files in /onion folder and its corresponding file in torrc directory
            FileUtils.deleteDirectory(new File(onionFilePath.toString()));
            Files.deleteIfExists(torrcOnionFilePath);

            // Call the shell script to delete Nginx configuration file and symbolic link
            ProcessBuilder processBuilder = new ProcessBuilder("shellScripts/remove_onion_files.sh", relayNickname);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Failed to delete Nginx configuration file and symbolic link");
            }

            nginxService.reloadNginx();

            response.put("success", true);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to remove Torrc file, DataDirectory, Onion files, Nginx configuration file and symbolic link for relayNickname: {}", relayNickname, e);
            response.put("success", false);
        }
        return response;
    }

    public Map<String, Object> openOrPort(String relayNickname, String relayType) {
        Map<String, Object> response = new HashMap<>();
        // Build the path to the torrc file
        Path torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType);

        // Get the orport from the torrc file
        int orPort = getOrPort(torrcFilePath);

        // Open the orport using UPnP
        boolean success = UPnP.openPortTCP(orPort);
        if (success) {
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("message", "Failed to open ORPort using UPnP");
        }
        return response;
    }

    public Map<String, Object> toggleUPnP(boolean enable) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get the list of all guard relays
            List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "guard");
            for (TorConfiguration config : guardConfigs) {
                if (enable) {
                    String status = relayStatusService.getRelayStatus(config.getGuardConfig().getNickname(), "guard");
                    if ("online".equals(status)) {
                        // Open the ORPort
                        openOrPort(config.getGuardConfig().getNickname(), "guard");
                    }
                } else {
                    // Close the ORPort
                    closeOrPort(config.getGuardConfig().getNickname(), "guard");
                }
            }
            response.put("success", true);
            response.put("message", "UPnP for Guard Relays " + (enable ? "enabled" : "disabled") + " successfully!");
        } catch (Exception e) {
            logger.error("Failed to " + (enable ? "enable" : "disable") + " UPnP for Guard Relays", e);
            response.put("success", false);
            response.put("message", "Failed to " + (enable ? "enable" : "disable") + " UPnP for Guard Relays.");
        }
        return response;
    }

    public void createDataDirectory() {
        try {
            Path dataDirectoryPath = Paths.get(System.getProperty("user.dir"), "torrc", "dataDirectory");
            if (!Files.exists(dataDirectoryPath)) {
                Files.createDirectory(dataDirectoryPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create dataDirectory folder", e);
        }
    }

    public String getRelayStatus(String relayNickname, String relayType) {
        return relayStatusService.getRelayStatus(relayNickname, relayType);
    }
}