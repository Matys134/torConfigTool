package com.school.torconfigtool.service;

import com.school.torconfigtool.models.BaseRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TorrcFileCreator {

    private static final Logger logger = LoggerFactory.getLogger(TorrcFileCreator.class);

    public static void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + config.getNickname());
            writer.newLine();
            writer.write("ORPort " + config.getOrPort());
            writer.newLine();
            writer.write("ContactInfo " + config.getContact());
            writer.newLine();
            writer.write("ControlPort " + config.getControlPort());
            writer.newLine();
            writer.write("SocksPort 0");
            writer.newLine();

            // Add any other common configurations from BaseRelayConfig
            String currentDirectory = System.getProperty("user.dir");
            String dataDirectoryPath = currentDirectory + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname();
            writer.write("DataDirectory " + dataDirectoryPath);

            // Use the new method to write specific configurations
            config.writeSpecificConfig(writer);

        } catch (IOException e) {
            logger.error("Error creating Torrc file", e);
        }
    }
}
