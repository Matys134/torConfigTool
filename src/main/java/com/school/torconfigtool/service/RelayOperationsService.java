package com.school.torconfigtool.service;

import com.school.torconfigtool.exception.RelayOperationException;
import com.school.torconfigtool.model.TorConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains methods to perform operations on Tor Relays.
 */
@Service
public class RelayOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsService.class);
    private final TorConfigService torConfigService;
    private final RelayOperationsService relayOperationsService;
    private final NginxService nginxService;
    private final OnionRelayOperationsService onionRelayOperationsService;
    private final TorFileService torFileService;
    private final RelayStatusService relayStatusService;
    private final UPnPService upnpService;
    private final BridgeRelayOperationsService bridgeRelayOperationsService;

    /**
     * Constructor for RelayOperationsService.
     *
     * @param torConfigService The TorConfigurationService to use.
     * @param relayOperationsService The RelayOperationsService to use.
     * @param nginxService The NginxService to use.
     * @param onionRelayOperationsService The OnionRelayOperationsService to use.
     * @param torFileService The TorFileService to use.
     * @param relayStatusService The RelayStatusService to use.
     * @param upnpService The UPnPService to use.
     * @param bridgeRelayOperationsService The BridgeRelayOperationsService to use.
     */
    public RelayOperationsService(TorConfigService torConfigService, RelayOperationsService relayOperationsService, NginxService nginxService, OnionRelayOperationsService onionRelayOperationsService, TorFileService torFileService, RelayStatusService relayStatusService, UPnPService upnpService, BridgeRelayOperationsService bridgeRelayOperationsService) {
        this.torConfigService = torConfigService;
        this.relayOperationsService = relayOperationsService;
        this.nginxService = nginxService;
        this.onionRelayOperationsService = onionRelayOperationsService;
        this.torFileService = torFileService;
        this.relayStatusService = relayStatusService;
        this.upnpService = upnpService;
        this.bridgeRelayOperationsService = bridgeRelayOperationsService;
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

    /**
     * This method starts a Tor Relay without updating the torrc file with fingerprints.
     *
     * @param relayNickname The nickname of the Tor Relay to start.
     * @param relayType The type of the Tor Relay to start.
     * @param model The Model to add attributes to.
     * @return String The name of the view to render.
     */
    public String changeRelayStateWithoutFingerprint(String relayNickname, String relayType, Model model) {
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);
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

    /**
     * This method starts a Tor Relay without updating the torrc file with fingerprints.
     *
     * @param torrcFilePath The path to the torrc file of the Tor Relay to start.
     * @param relayNickname The nickname of the Tor Relay to start.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to finish.
     */
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

    /**
     * This method starts or stops a Tor Relay.
     *
     * @param relayNickname The nickname of the Tor Relay to start or stop.
     * @param relayType The type of the Tor Relay to start or stop.
     * @param model The Model to add attributes to.
     * @param start Whether to start or stop the Tor Relay.
     * @return String The name of the view to render.
     */
    public String changeRelayState(String relayNickname, String relayType, Model model, boolean start) {
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);
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

    /**
     * This method starts or stops a Tor Relay.
     *
     * @param torrcFilePath The path to the torrc file of the Tor Relay to start or stop.
     * @param relayNickname The nickname of the Tor Relay to start or stop.
     * @param start Whether to start or stop the Tor Relay.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to finish.
     */
    private void processRelayOperation(Path torrcFilePath, String relayNickname, boolean start) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RelayOperationException("Torrc file does not exist for relay: " + relayNickname);
        }
        if (start) {
            // Step 1: Retrieve Fingerprints
            List<String> allFingerprints = getAllRelayFingerprints();

            // Step 2: Update the torrc File with fingerprints
            torFileService.updateTorrcWithFingerprints(torrcFilePath, allFingerprints);

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

    /**
     * This method retrieves fingerprints from all existing relays.
     *
     * @param dataDirectoryPath The path to the data directory of the Tor Relay.
     * @return List The list of fingerprints.
     */
    private List<String> getFingerprints(String dataDirectoryPath) {
        List<String> fingerprints = new ArrayList<>();
        File dataDirectory = new File(dataDirectoryPath);
        File[] dataDirectoryFiles = dataDirectory.listFiles(File::isDirectory);

        if (dataDirectoryFiles != null) {
            for (File dataDir : dataDirectoryFiles) {
                String fingerprintFilePath = dataDir.getAbsolutePath() + File.separator + "fingerprint";
                String fingerprint = torFileService.readFingerprint(fingerprintFilePath);
                if (fingerprint != null) {
                    fingerprints.add(fingerprint);
                }
            }
        }
        return fingerprints;
    }

    /**
     * This method retrieves fingerprints from all existing relays.
     *
     * @return List The list of fingerprints.
     */
    private List<String> getAllRelayFingerprints() {
        // This path should lead to the base directory where all relay data directories are stored
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        return getFingerprints(dataDirectoryPath);
    }


    public String relayOperations(Model model) {
        System.out.println("Inside relayOperations method");
        String folderPath = torConfigService.buildFolderPath();
        model.addAttribute("guardConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "guard"));

        model.addAttribute("bridgeConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "bridge"));

        model.addAttribute("onionConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "onion"));
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurationsFromFolder(folderPath, "onion");

        logger.info("OnionConfigs: {}", onionConfigs);

        // Create a map to store hostnames for onion services
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfig config : onionConfigs) {
            String hostname = onionRelayOperationsService.readHostnameFile(config.getHiddenServicePort());
            hostnames.put(config.getHiddenServicePort(), hostname);
            logger.info("Hostname for port {}: {}", config.getHiddenServicePort(), hostname);
        }

        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurationsFromFolder(folderPath, "bridge");
        Map<String, String> webtunnelLinks = new HashMap<>();
        for (TorConfig config : bridgeConfigs) {
            String webtunnelLink = bridgeRelayOperationsService.getWebtunnelLink(config.getBridgeConfig().getNickname());
            webtunnelLinks.put(config.getBridgeConfig().getNickname(), webtunnelLink);
            logger.info("Added webtunnel link for " + config.getBridgeConfig().getNickname() + ": " + webtunnelLink);
        }
        model.addAttribute("webtunnelLinks", webtunnelLinks);

        logger.info("Hostnames: {}", hostnames);
        model.addAttribute("hostnames", hostnames);

        List<Integer> upnpPorts = upnpService.getUPnPPorts();
        model.addAttribute("upnpPorts", upnpPorts);

        return "relay-operations";
    }

    /**
     * This method stops a Tor Relay.
     * @param relayNickname The nickname of the Tor Relay to stop.
     * @param relayType The type of the Tor Relay to stop.
     * @param model The Model to add attributes to.
     * @return String The name of the view to render.
     */
    public String stopRelay(String relayNickname, String relayType, Model model) {
        String view = changeRelayState(relayNickname, relayType, model, false);

        new Thread(() -> {
            try {
                relayStatusService.waitForStatusChange(relayNickname, relayType, "offline");
                // Close the ORPort after the relay has stopped
                upnpService.closeOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to stop", e);
            }
        }).start();

        return view;
    }

    /**
     * This method starts a Tor Relay.
     * @param relayNickname The nickname of the Tor Relay to start.
     * @param relayType The type of the Tor Relay to start.
     * @param model The Model to add attributes to.
     * @return String The name of the view to render.
     */
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
                relayStatusService.waitForStatusChange(relayNickname, relayType, "online");
                upnpService.openOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to start", e);
            }
        }).start();
        System.out.println("Returning view");

        return view;
    }

    /**
     * This method removes a Tor Relay.
     * @param relayNickname The nickname of the Tor Relay to remove.
     * @param relayType The type of the Tor Relay to remove.
     * @return String The name of the view to render.
     */
    public Map<String, Object> removeRelay(String relayNickname, String relayType) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Build paths for Torrc file and DataDirectory
            Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);
            String dataDirectoryPath = torFileService.buildDataDirectoryPath(relayNickname);

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

    /**
     * This method creates a DataDirectory folder for the Tor Relay.
     */
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
    // openOrPort and closeOrPort methods that take from the UPnPService class
    public Map<String, Object> openOrPort(String relayNickname, String relayType) {
        return upnpService.openOrPort(relayNickname, relayType);
    }

    // toggleUPnP method that takes from the UPnPService class
    public Map<String, Object> toggleUPnP(boolean enable) {
        return upnpService.toggleUPnP(enable);
    }
}