package com.school.torconfigtool.service;

import com.school.torconfigtool.models.RelayConfig;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.models.GuardRelayConfig;
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
        return "torrc" + File.separator + relayType;
    }

    private List<TorConfiguration> readTorConfigurationsFromFolder(String folderPath, String relayType) {
        List<TorConfiguration> configs = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    TorConfiguration config = parseTorConfiguration(file, relayType);
                    configs.add(config);
                } catch (IOException e) {
                    logger.error("Error reading Tor configuration file: {}", file.getName(), e);
                }
            }
        }

        return configs;
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
            config.setHiddenServicePort(line.split(" ")[1].trim());
        } else if (line.startsWith("ControlPort")) {
            relayConfig.setControlPort(line.split(" ")[1].trim());
        } else if (line.startsWith("BandwidthRate")) {
            config.setBandwidthRate(line.split(" ")[1].trim());
        } else if (line.startsWith("ServerTransportListenAddr obfs4") && relayType.equals("bridge")) {
            ((BridgeRelayConfig) relayConfig).setBridgeTransportListenAddr(line.substring(line.indexOf("obfs4")).trim());
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
