package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
import com.simtechdata.waifupnp.UPnP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling UPnP related operations.
 */
@Service
public class UPnPService {

    private final TorFileService torFileService;
    private final TorConfigService torConfigService;
    private final RelayStatusService relayStatusService;

    private static final Logger logger = LoggerFactory.getLogger(UPnPService.class);

    /**
     * Constructor for UPnPService.
     *
     * @param torFileService      Service for handling Tor file operations.
     * @param torConfigService    Service for handling Tor configuration operations.
     * @param relayStatusService  Service for handling relay status operations.
     */
    public UPnPService(TorFileService torFileService, TorConfigService torConfigService, RelayStatusService relayStatusService) {
        this.torFileService = torFileService;
        this.torConfigService = torConfigService;
        this.relayStatusService = relayStatusService;
    }

    /**
     * Opens or closes a port based on the relay nickname and type.
     *
     * @param relayNickname  The nickname of the relay.
     * @param relayType      The type of the relay.
     * @return               A map containing the success status and a message.
     */
    public Map<String, Object> openOrPort(String relayNickname, String relayType) {
        Map<String, Object> response = new HashMap<>();
        // Build the path to the torrc file
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);

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

    /**
     * Toggles UPnP on or off.
     *
     * @param enable  A boolean indicating whether to enable or disable UPnP.
     * @return        A map containing the success status and a message.
     */
    public Map<String, Object> toggleUPnP(boolean enable) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get the list of all guard relays
            List<TorConfig> guardConfigs = torConfigService.readTorConfigurationsFromFolder(torConfigService.buildFolderPath(), "guard");
            for (TorConfig config : guardConfigs) {
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

    /**
     * Closes a port based on the relay nickname and type.
     *
     * @param relayNickname  The nickname of the relay.
     * @param relayType      The type of the relay.
     */
    public void closeOrPort(String relayNickname, String relayType) {
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);
        int orPort = getOrPort(torrcFilePath);
        UPnP.closePortTCP(orPort);
    }

    /**
     * Retrieves the ORPort from a torrc file.
     *
     * @param torrcFilePath  The path to the torrc file.
     * @return               The ORPort as an integer.
     */
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

    /**
     * Retrieves a list of UPnP ports.
     *
     * @return  A list of UPnP ports as integers.
     */
    public List<Integer> getUPnPPorts() {
        List<Integer> upnpPorts = new ArrayList<>();
        List<TorConfig> guardConfigs = torConfigService.readTorConfigurationsFromFolder(torConfigService.buildFolderPath(), "guard");
        for (TorConfig config : guardConfigs) {
            int orPort = getOrPort(torFileService.buildTorrcFilePath(config.getGuardConfig().getNickname(), "guard"));
            if (UPnP.isMappedTCP(orPort)) {
                upnpPorts.add(orPort);
            }
        }
        return upnpPorts;
    }
}