package com.school.torconfigtool.service;


import com.school.torconfigtool.model.TorConfig;
import org.apache.commons.io.FileUtils;
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

import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * This class contains methods to perform operations on Tor Relays.
 */
@Service
public class RelayOperationsService {

    private final TorConfigService torConfigService;
    private final NginxService nginxService;
    private final TorFileService torFileService;
    private final RelayStatusService relayStatusService;
    private final UPnPService upnpService;
    private final WebtunnelService webtunnelService;
    private final OnionService onionService;
    private final CommandService commandService;

    /**
     * Constructor for RelayOperationsService.
     *
     * @param torConfigService The TorConfigurationService to use.
     * @param nginxService The NginxService to use.
     * @param torFileService The TorFileService to use.
     * @param relayStatusService The RelayStatusService to use.
     * @param upnpService The UPnPService to use.
     */
    public RelayOperationsService(TorConfigService torConfigService, NginxService nginxService, TorFileService torFileService, RelayStatusService relayStatusService, UPnPService upnpService, WebtunnelService webtunnelService, OnionService onionService, CommandService commandService) {
        this.torConfigService = torConfigService;
        this.nginxService = nginxService;
        this.torFileService = torFileService;
        this.relayStatusService = relayStatusService;
        this.upnpService = upnpService;
        this.webtunnelService = webtunnelService;
        this.onionService = onionService;
        this.commandService = commandService;
    }

    public String changeRelayState(String relayNickname, String relayType, Model model, boolean start, boolean updateFingerprint) {
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);
        String operation = start ? "start" : "stop";
        try {
            processRelayOperation(torrcFilePath, relayNickname, start, updateFingerprint);
            model.addAttribute("successMessage", "Tor Relay " + operation + "ed successfully!");
        } catch (RuntimeException | IOException | InterruptedException e) {
            model.addAttribute("errorMessage", "Failed to " + operation + " Tor Relay.");
        }
        return prepareModelForRelayOperationsView(model);
    }

    private void processRelayOperation(Path torrcFilePath, String relayNickname, boolean start, boolean updateFingerprint) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RuntimeException("Torrc file does not exist for relay: " + relayNickname);
        }
        if (start) {
            if (updateFingerprint) {
                // Step 1: Retrieve Fingerprints
                List<String> allFingerprints = getAllRelayFingerprints();

                // Step 2: Update the torrc File with fingerprints
                torFileService.updateTorrcWithFingerprints(torrcFilePath, allFingerprints);
            }

            // Step 3: Start the Relay
            String command = "tor -f " + torrcFilePath.toAbsolutePath();
            try {
                commandService.executeCommand(command);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to start Tor Relay service.", e);
            }

        } else {
            int pid = relayStatusService.getTorRelayPID(torrcFilePath.toString());
            if (pid > 0) {
                String command = "kill -SIGINT " + pid;
                try {
                    commandService.executeCommand(command);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Failed to stop Tor Relay service.", e);
                }
            } else if (pid == -1) {
                throw new RuntimeException("Tor Relay is not running.");
            } else {
                throw new RuntimeException("Error occurred while retrieving PID for Tor Relay.");
            }
        }
    }

    /**
     * This method retrieves fingerprints from guard relays.
     *
     * @return List The list of fingerprints.
     */
    private List<String> getAllRelayFingerprints() {
        // This path should lead to the base directory where all relay data directories are stored
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        File dataDirectory = new File(dataDirectoryPath);
        File[] dataDirectoryFiles = dataDirectory.listFiles(File::isDirectory);

        List<String> fingerprints = new ArrayList<>();
        if (dataDirectoryFiles != null) {
            for (File dataDir : dataDirectoryFiles) {
                // Check if the directory name ends with "_guard" to ensure it's a guard relay
                if (dataDir.getName().endsWith("_GuardConfig")) {
                    String fingerprintFilePath = dataDir.getAbsolutePath() + File.separator + "fingerprint";
                    String fingerprint = torFileService.readFingerprint(fingerprintFilePath);
                    if (fingerprint != null) {
                        fingerprints.add(fingerprint);
                    }
                }
            }
        }
        return fingerprints;
    }


    public String prepareModelForRelayOperationsView(Model model) {
        String folderPath = torConfigService.buildFolderPath();
        model.addAttribute("guardConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "guard"));

        model.addAttribute("bridgeConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "bridge"));

        model.addAttribute("onionConfigs", torConfigService.readTorConfigurationsFromFolder(folderPath, "onion"));
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurationsFromFolder(folderPath, "onion");

        // Create a map to store hostnames for onion services
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfig config : onionConfigs) {
            String hostname = onionService.readHostnameFile(Integer.parseInt(config.getHiddenServicePort()));
            hostnames.put(config.getHiddenServicePort(), hostname);
        }

        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurationsFromFolder(folderPath, "bridge");
        Map<String, String> webtunnelLinks = new HashMap<>();
        for (TorConfig config : bridgeConfigs) {
            String webtunnelLink = webtunnelService.getWebtunnelLink(config.getBridgeConfig().getNickname(), config.getBridgeConfig().getWebtunnelPort());
            webtunnelLinks.put(config.getBridgeConfig().getNickname(), webtunnelLink);
        }
        model.addAttribute("webtunnelLinks", webtunnelLinks);

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
        String view = changeRelayState(relayNickname, relayType, model, false, true);

        new Thread(() -> {
            try {
                relayStatusService.waitForStatusChange(relayNickname, relayType, "offline");
                // Close the ORPort after the relay has stopped
                upnpService.closeOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while waiting for relay to stop", e);
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
            view = changeRelayState(relayNickname, relayType, model, true, true);
        } else {
            view = changeRelayState(relayNickname, relayType, model, true, false);
        }

        new Thread(() -> {
            try {
                relayStatusService.waitForStatusChange(relayNickname, relayType, "online");
                upnpService.openOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while waiting for relay to start", e);
            }
        }).start();

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

            String capitalizedRelayType = relayType.substring(0, 1).toUpperCase() + relayType.substring(1);

            String dataDirectoryPath = torFileService.buildDataDirectoryPath(relayNickname + "_" + capitalizedRelayType + "Config");

            // Delete Torrc file
            Files.deleteIfExists(torrcFilePath);

            // Delete DataDirectory
            FileUtils.deleteDirectory(new File(dataDirectoryPath));

            // Build paths for Onion files in /onion folder and its corresponding file in torrc directory
            Path onionFilePath = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs", "onion-service-" + relayNickname);
            Path torrcOnionFilePath = Paths.get(System.getProperty("user.dir"), "torrc", TORRC_FILE_PREFIX + relayNickname + "_onion");

            // Delete Onion files in /onion folder and its corresponding file in torrc directory
            FileUtils.deleteDirectory(new File(onionFilePath.toString()));
            Files.deleteIfExists(torrcOnionFilePath);

            onionService.removeOnionFiles(relayNickname);

            nginxService.reloadNginx();

            response.put("success", true);
        } catch (IOException | InterruptedException e) {
            response.put("success", false);
        }
        return response;
    }

    public String getRelayStatus(String relayNickname, String relayType) {
        return relayStatusService.getRelayStatus(relayNickname, relayType);
    }
}