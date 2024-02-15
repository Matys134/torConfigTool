package com.school.torconfigtool;

import com.school.torconfigtool.bridge.config.BridgeRelayConfig;
import com.school.torconfigtool.guard.config.GuardRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TorConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(TorConfigurationService.class);

    public List<TorConfiguration> readTorConfigurations() {
        List<TorConfiguration> configs = new ArrayList<>();
        String folderPath = buildFolderPath();

        configs.addAll(readTorConfigurationsFromFolder(folderPath, "guard"));
        configs.addAll(readTorConfigurationsFromFolder(folderPath, "bridge"));

        return configs;
    }

    public String buildFolderPath() {
        return "torrc";
    }

    public List<TorConfiguration> readTorConfigurationsFromFolder(String folderPath, String expectedRelayType) {
        List<TorConfiguration> configs = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                String relayType = parseRelayTypeFromFile(file);
                if (relayType.equals(expectedRelayType)) {
                    try {
                        TorConfiguration config = parseTorConfiguration(file, relayType);
                        configs.add(config);
                    } catch (IOException e) {
                        logger.error("Error reading Tor configuration file: {}", file.getName(), e);
                    }
                }
            }
        }

        return configs;
    }

    private String parseRelayTypeFromFile(File file) {
        String fileName = file.getName();
        // e.g. assuming file name is "torrc-relayNickname_relayType"
        return fileName.substring(fileName.indexOf("_") + 1);
    }

    private TorConfiguration parseTorConfiguration(File file, String relayType) throws IOException {
        TorConfiguration config = new TorConfiguration();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseTorConfigLine(line, config, relayType);
            }
        }
        return config;
    }

    private void parseTorConfigLine(String line, TorConfiguration config, String relayType) {
        RelayConfig relayConfig = getRelayConfig(config, relayType);

        if (line.startsWith("Nickname")) {
            relayConfig.setNickname(line.split(" ")[1].trim());
        } else if (line.startsWith("ORPort")) {
            relayConfig.setOrPort(line.split(" ")[1].trim());
        } else if (line.startsWith("Contact")) {
            relayConfig.setContact(line.split(" ")[1].trim());
        } else if (line.startsWith("HiddenServiceDir")) {
            config.setHiddenServiceDir(line.split(" ")[1].trim());
        } else if (line.startsWith("HiddenServicePort")) {
            String[] parts = line.split(" ");
            String addressAndPort = parts[parts.length - 1]; // Get the last element "127.0.0.1:9005"
            String port = addressAndPort.split(":")[1]; // Split by ":" and get the second element "9005"
            config.setHiddenServicePort(port);
        } else if (line.startsWith("ControlPort")) {
            relayConfig.setControlPort(line.split(" ")[1].trim());
        } else if (line.startsWith("BandwidthRate")) {
            config.setBandwidthRate(line.split(" ")[1].trim());
        } else if (line.startsWith("ServerTransportListenAddr obfs4") && relayType.equals("bridge")) {
            ((BridgeRelayConfig) relayConfig).setServerTransport(line.substring(line.indexOf("obfs4")).trim());
        } else if (line.startsWith("ServerTransportOptions webtunnel url") && relayType.equals("bridge")) {
                String fullUrl = line.split("=")[1].trim();
                try {
                    java.net.URL url = new java.net.URL(fullUrl);
                    String webtunnelUrl = url.getHost();
                    String path = url.getPath().substring(1); // Remove the leading "/"
                    ((BridgeRelayConfig) relayConfig).setWebtunnelUrl(webtunnelUrl);
                    ((BridgeRelayConfig) relayConfig).setPath(path);
                } catch (java.net.MalformedURLException e) {
                    logger.error("Invalid webtunnel URL: " + fullUrl, e);
                }
            }
    }

    private RelayConfig getRelayConfig(TorConfiguration config, String relayType) {
        RelayConfig relayConfig = null;

        if ("guard".equals(relayType)) {
            if (config.getGuardRelayConfig() == null) {
                config.setGuardRelayConfig(new GuardRelayConfig());
            }
            relayConfig = config.getGuardRelayConfig();
        } else if ("bridge".equals(relayType)) {
            if (config.getBridgeRelayConfig() == null) {
                config.setBridgeRelayConfig(new BridgeRelayConfig());
            }
            relayConfig = config.getBridgeRelayConfig();
        }

        return relayConfig;
    }
}
