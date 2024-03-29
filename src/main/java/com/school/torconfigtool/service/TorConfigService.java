package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.model.RelayConfig;
import com.school.torconfigtool.model.TorConfig;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Tor configurations.
 */
@Service
public class TorConfigService {

    /**
     * Reads Tor configurations from a specified folder and relay type.
     *
     * @param folderPath the path to the folder.
     * @param expectedRelayType the expected relay type.
     * @return a list of Tor configurations.
     */
    public List<TorConfig> readTorConfigurations(String folderPath, String expectedRelayType) {
        System.out.println("Reading configurations from: " + folderPath);
        List<TorConfig> configs = new ArrayList<>();
        System.out.println("Expected relay type: " + expectedRelayType);
        System.out.println("Reading configurations...");
        System.out.println(configs);
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                String relayType = parseRelayTypeFromFile(file);
                if (relayType.equals(expectedRelayType)) {
                    try {
                        TorConfig config = parseTorConfig(file, relayType);
                        configs.add(config);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return configs;
    }

    /**
     * Parses the relay type from the file name.
     *
     * @param file the file to parse.
     * @return the relay type as a string.
     */
    private String parseRelayTypeFromFile(File file) {
        String fileName = file.getName();
        // e.g., assuming file name is "torrc-relayNickname_relayType"
        return fileName.substring(fileName.indexOf("_") + 1);
    }

    /**
     * Parses a Tor configuration from a file.
     *
     * @param file the file to parse.
     * @param relayType the relay type.
     * @return a Tor configuration.
     * @throws IOException if an I/O error occurs.
     */
    private TorConfig parseTorConfig(File file, String relayType) throws IOException {
        TorConfig config = new TorConfig();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseTorConfigLines(line, config, relayType);
            }
        }
        return config;
    }

    /**
     * Parses a line from a Tor configuration file and updates the configuration accordingly.
     *
     * @param line the line to parse.
     * @param config the configuration to update.
     * @param relayType the relay type.
     */
    private void parseTorConfigLines(String line, TorConfig config, String relayType) {
        RelayConfig relayConfig = getRelayConfig(config, relayType);

        if (line.startsWith("Nickname")) {
            relayConfig.setNickname(line.split(" ")[1].trim());
        } else if (line.startsWith("ORPort")) {
            relayConfig.setOrPort(line.split(" ")[1].trim());
        } else if (line.startsWith("Contact")) {
            relayConfig.setContact(line.split(" ")[1].trim());
        } else if (line.startsWith("HiddenServiceDir")) {
            config.getOnionConfig().setHiddenServiceDir(line.split(" ")[1].trim());
        } else if (line.startsWith("HiddenServicePort")) {
            String[] parts = line.split(" ");
            String addressAndPort = parts[parts.length - 1];
            String port = addressAndPort.split(":")[1];
            config.getOnionConfig().setHiddenServicePort(port);
        } else if (line.startsWith("ControlPort")) {
            relayConfig.setControlPort(line.split(" ")[1].trim());
        } else if (line.startsWith("RelayBandwidthRate")) {
            String bandwidthRate = line.substring("RelayBandwidthRate".length()).trim();
            updateBandwidthRate(relayConfig, bandwidthRate);
        } else if ((line.startsWith("ServerTransportListenAddr obfs4") || line.startsWith("ServerTransportListenAddr webtunnel")) && relayType.equals("bridge")) {
            String port = line.split(" ")[2].split(":")[1];
            ((BridgeConfig) relayConfig).setServerTransport(port);
        } else if (line.startsWith("ServerTransportOptions webtunnel url") && relayType.equals("bridge")) {
            String fullUrl = line.split("=")[1].trim();
            try {
                java.net.URI uri = new java.net.URI(fullUrl);
                String webtunnelUrl = uri.getHost();
                String path = uri.getPath().substring(1);
                ((BridgeConfig) relayConfig).setWebtunnelUrl(webtunnelUrl);
                ((BridgeConfig) relayConfig).setPath(path);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else if (line.startsWith("# webtunnel")) {
            ((BridgeConfig) relayConfig).setWebtunnelPort(Integer.parseInt(line.split(" ")[2]));
        }
    }

    /**
     * Gets the relay configuration from a Tor configuration based on the relay type.
     *
     * @param config the Tor configuration.
     * @param relayType the relay type.
     * @return the relay configuration.
     */
    private RelayConfig getRelayConfig(TorConfig config, String relayType) {
        RelayConfig relayConfig = null;

        if ("guard".equals(relayType)) {
            if (config.getGuardConfig() == null) {
                config.setGuardConfig(new GuardConfig());
            }
            relayConfig = config.getGuardConfig();
        } else if ("bridge".equals(relayType)) {
            if (config.getBridgeConfig() == null) {
                config.setBridgeConfig(new BridgeConfig());
            }
            relayConfig = config.getBridgeConfig();
        }

        return relayConfig;
    }

    private void updateBandwidthRate(RelayConfig relayConfig, String bandwidthRate) {
        relayConfig.setBandwidthRate(bandwidthRate);
    }
}