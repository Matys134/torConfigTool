package com.school.torconfigtool.service;

import com.school.torconfigtool.models.TorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class TorConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(TorConfigurationService.class);

    public List<TorConfiguration> readTorConfigurations(String relayType) {
        String folderPath = buildFolderPath(relayType);
        return readTorConfigurationsFromFolder(folderPath, relayType);
    }

    private String buildFolderPath(String relayType) {
        // Assumes the folder is either "guard" or "bridge", may need validation
        return "torrc" + File.separator + relayType;
    }

    private List<TorConfiguration> readTorConfigurationsFromFolder(String folderPath, String relayType) {
        List<TorConfiguration> configs = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    TorConfiguration config = parseTorConfiguration(file);
                    config.setRelayType(relayType); // Set the relay type
                    configs.add(config);
                } catch (IOException e) {
                    logger.error("Error reading Tor configuration file: {}", file.getName(), e);
                }
            }
        }

        return configs;
    }

    private TorConfiguration parseTorConfiguration(File file) throws IOException {
        TorConfiguration config = new TorConfiguration();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseTorConfigLine(line, config);
            }
        }
        return config;
    }

    private void parseTorConfigLine(String line, TorConfiguration config) {
        if (line.startsWith("Nickname")) {
            config.setNickname(line.split("Nickname")[1].trim());
        } else if (line.startsWith("ORPort")) {
            config.setOrPort(line.split("ORPort")[1].trim());
        } else if (line.startsWith("Contact")) {
            config.setContact(line.split("Contact")[1].trim());
        } else if (line.startsWith("HiddenServiceDir")) {
            // Onion service specific
            config.setHiddenServiceDir(line.split("HiddenServiceDir")[1].trim());
        } else if (line.startsWith("HiddenServicePort")) {
            // Onion service specific
            config.setHiddenServicePort(line.split("HiddenServicePort")[1].trim());
        } else if (line.startsWith("ControlPort")) {
            config.setControlPort(line.split("ControlPort")[1].trim());
        } else if (line.startsWith("SocksPort")) {
            config.setSocksPort(line.split("SocksPort")[1].trim());
        } else if (line.startsWith("BandwidthRate")) {
            config.setBandwidthRate(line.split("BandwidthRate")[1].trim());
        } else if (line.startsWith("ServerTransportListenAddr obfs4 0.0.0.0:")) {
            // Bridge specific
            config.setBridgeTransportListenAddr(line.split("ServerTransportListenAddr obfs4")[1].trim());
        }
    }
}
