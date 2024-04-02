package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.util.Constants;
import com.simtechdata.waifupnp.UPnP;
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
    public Map<String, Object> openPorts(String relayNickname, String relayType) {
        Map<String, Object> response = new HashMap<>();
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);

        Map<String, List<Integer>> ports = getPorts(torrcFilePath);
        boolean success = true;

        for (int port : ports.get("ORPort")) {
            success &= UPnP.openPortTCP(port);
        }

        for (int port : ports.get("Obfs4Port")) {
            success &= UPnP.openPortTCP(port);
        }

        for (int port : ports.get("WebTunnelPort")) {
            success &= UPnP.openPortTCP(port);
        }

        if (success) {
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("message", "Failed to open ORPort using UPnP");
        }
        return response;
    }

    /**
     * Closes a port based on the relay nickname and type.
     *
     * @param relayNickname  The nickname of the relay.
     * @param relayType      The type of the relay.
     */
    public void closePorts(String relayNickname, String relayType) {
        Path torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType);

        Map<String, List<Integer>> ports = getPorts(torrcFilePath);
        ports.get("ORPort").forEach(port -> {
            if (UPnP.isMappedTCP(port)) {
                UPnP.closePortTCP(port);
            }
        });

        // Close ServerTransportListenAddr ports and webtunnel ports
        for (int port : ports.get("Obfs4Port")) {
            if (UPnP.isMappedTCP(port)) {
                UPnP.closePortTCP(port);
            }
        }

        for (int port : ports.get("WebTunnelPort")) {
            if (UPnP.isMappedTCP(port)) {
                UPnP.closePortTCP(port);
            }
        }
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
            List<TorConfig> guardConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH, "guard");
            List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH, "bridge");
            List<TorConfig> allConfigs = new ArrayList<>();
            allConfigs.addAll(guardConfigs);
            allConfigs.addAll(bridgeConfigs);

            for (TorConfig config : allConfigs) {
                String relayType = null;
                String relayNickname = null;
                if (config.getGuardConfig() != null) {
                    relayType = "guard";
                    relayNickname = config.getGuardConfig().getNickname();
                } else if (config.getBridgeConfig() != null) {
                    relayType = "bridge";
                    relayNickname = config.getBridgeConfig().getNickname();
                }

                if (relayType != null && relayNickname != null) {
                    if (enable) {
                        String status = relayStatusService.getRelayStatus(relayNickname, relayType);
                        if ("online".equals(status)) {
                            openPorts(relayNickname, relayType);
                        }
                    } else {
                        closePorts(relayNickname, relayType);
                    }
                }
            }
            response.put("success", true);
            response.put("message", "UPnP for Guard and Bridge Relays " + (enable ? "enabled" : "disabled") + " successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to " + (enable ? "enable" : "disable") + " UPnP for Guard and Bridge Relays.");
        }
        return response;
    }

    public Map<String, List<Integer>> getPorts(Path torrcFilePath) {
        Map<String, List<Integer>> ports = new HashMap<>();
        ports.put("ORPort", new ArrayList<>());
        ports.put("Obfs4Port", new ArrayList<>());
        ports.put("WebTunnelPort", new ArrayList<>());

        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))){
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ORPort")) {
                    if (!line.equals("ORPort 127.0.0.1:auto IPv4Only")) {
                        String portStr = line.split(" ")[1];
                        ports.get("ORPort").add(Integer.parseInt(portStr));
                    }
                }
                if (line.startsWith("ServerTransportListenAddr obfs4")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 2) {
                        String[] addrParts = parts[2].split(":");
                        if (addrParts.length > 1) {
                            ports.get("Obfs4Port").add(Integer.parseInt(addrParts[1]));
                        }
                    }
                }
                if (line.startsWith("# webtunnel")) {
                    ports.get("WebTunnelPort").add(Integer.parseInt(line.split(" ")[2]));
                    System.out.println("WebTunnelPort: " + line.split(" ")[2]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read torrc file", e);
        }
        return ports;
    }

    public boolean isUPnPAvailable() {
        return UPnP.isUPnPAvailable();
    }
}
