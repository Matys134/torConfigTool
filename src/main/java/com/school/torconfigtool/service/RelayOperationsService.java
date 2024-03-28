package com.school.torconfigtool.service;


import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.util.Constants;
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
    private final Obfs4Service obfs4Service;

    /**
     * Constructor for RelayOperationsService.
     *
     * @param torConfigService The TorConfigurationService to use.
     * @param nginxService The NginxService to use.
     * @param torFileService The TorFileService to use.
     * @param relayStatusService The RelayStatusService to use.
     * @param upnpService The UPnPService to use.
     */
    public RelayOperationsService(TorConfigService torConfigService, NginxService nginxService, TorFileService torFileService, RelayStatusService relayStatusService, UPnPService upnpService, WebtunnelService webtunnelService, OnionService onionService, CommandService commandService, Obfs4Service obfs4Service) {
        this.torConfigService = torConfigService;
        this.nginxService = nginxService;
        this.torFileService = torFileService;
        this.relayStatusService = relayStatusService;
        this.upnpService = upnpService;
        this.webtunnelService = webtunnelService;
        this.onionService = onionService;
        this.commandService = commandService;
        this.obfs4Service = obfs4Service;
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
            // If the relay is a guard relay, update the torrc file with fingerprints
            if (updateFingerprint) {
                //Retrieve Fingerprints
                List<String> allFingerprints = getGuardRelayFingerprints();

                //Update the torrc File with fingerprints
                torFileService.updateTorrcWithFingerprints(torrcFilePath, allFingerprints);
            }

            //Start the Relay
            String command = "sudo tor -f " + torrcFilePath.toAbsolutePath();
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
    private List<String> getGuardRelayFingerprints() {
        // This path lead to the base directory where all relay data directories are stored
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator
                + "dataDirectory";
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
        addRelayConfigsToModel(model);
        addHostnamesToModel(model);
        addWebtunnelLinksToModel(model);
        addObfs4LinksToModel(model);
        return "relay-operations";
    }

    private void addRelayConfigsToModel(Model model) {
        model.addAttribute("guardConfigs", torConfigService.readTorConfigurations
                (Constants.TORRC_DIRECTORY_PATH, "guard"));
        model.addAttribute("bridgeConfigs", torConfigService.readTorConfigurations
                (Constants.TORRC_DIRECTORY_PATH, "bridge"));
        model.addAttribute("onionConfigs", torConfigService.readTorConfigurations
                (Constants.TORRC_DIRECTORY_PATH, "onion"));
    }

    private void addHostnamesToModel(Model model) {
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH, "onion");
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfig config : onionConfigs) {
            String hostname = onionService.readHostnameFile(Integer.parseInt(config.getHiddenServicePort()));
            hostnames.put(config.getHiddenServicePort(), hostname);
        }
        model.addAttribute("hostnames", hostnames);
    }

    private void addWebtunnelLinksToModel(Model model) {
        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH, "bridge");
        Map<String, String> webtunnelLinks = new HashMap<>();
        for (TorConfig config : bridgeConfigs) {
            String webtunnelLink = webtunnelService.getWebtunnelLink(config.getBridgeConfig().getNickname());
            webtunnelLinks.put(config.getBridgeConfig().getNickname(), webtunnelLink);
        }
        model.addAttribute("webtunnelLinks", webtunnelLinks);
    }

    private void addObfs4LinksToModel(Model model) {
        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH, "bridge");
        Map<String, String> obfs4Links = new HashMap<>();
        for (TorConfig config : bridgeConfigs) {
            String obfs4Link = obfs4Service.getObfs4Link(config.getBridgeConfig().getNickname(), config.getBridgeConfig());
            obfs4Links.put(config.getBridgeConfig().getNickname(), obfs4Link);
        }
        model.addAttribute("obfs4Links", obfs4Links);
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
                upnpService.closePorts(relayNickname, relayType);
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
                upnpService.openPorts(relayNickname, relayType);
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