package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for creating a Torrc file based on the provided configuration.
 */
public class TorrcFileCreator {

    /**
     * Creates a Torrc file based on the provided configuration.
     * @param filePath The path to the file to be created.
     * @param config The configuration to be used for creating the file.
     */
    public static void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            TorrcWriteConfigService torrcWriteConfigService = new TorrcWriteConfigService();
            torrcWriteConfigService.writeTorrcFileConfig(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Torrc file", e);
        }
    }
}