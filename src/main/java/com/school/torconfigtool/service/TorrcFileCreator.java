package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for creating a Torrc file based on the provided configuration.
 */
public class TorrcFileCreator {

    /**
     * Creates a Torrc file at the specified file path with the provided configuration.
     *
     * @param filePath The path where the Torrc file will be created.
     * @param config   The configuration to be written to the Torrc file.
     */
    public static void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the nickname to the file
            writer.write("Nickname " + config.getNickname());
            writer.newLine();

            // Write the ORPort to the file, defaulting to "127.0.0.1:auto" if not provided
            String orPort = config.getOrPort() != null ? config.getOrPort() : "127.0.0.1:auto";
            writer.write("ORPort " + orPort + " IPv4Only");
            writer.newLine();

            // Write the contact info to the file
            writer.write("ContactInfo " + config.getContact());
            writer.newLine();

            // Write the control port to the file
            writer.write("ControlPort " + config.getControlPort());
            writer.newLine();

            writer.write("CookieAuthentication 1");
            writer.newLine();

            // Write the SocksPort to the file
            writer.write("SocksPort 0");
            writer.newLine();

            // Write the RunAsDaemon configuration to the file
            writer.write("RunAsDaemon 1");
            writer.newLine();

            // Write the DataDirectory configuration to the file
            String relayType = config.getClass().getSimpleName(); // Get the class name as the relay type
            String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname() + "_" + relayType;
            writer.write("DataDirectory " + dataDirectoryPath);
            writer.newLine();

            // Write the BandwidthRate to the file if provided
            if (config.getBandwidthRate() != null) {
                writer.write("RelayBandwidthRate " + config.getBandwidthRate() + " KBytes");
                writer.newLine();
            }

            // Write specific configuration based on the type of relay
            RelayWriteConfigService relayWriteConfigService = new RelayWriteConfigService();
            relayWriteConfigService.writeConfig(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Torrc file", e);
        }
    }
}