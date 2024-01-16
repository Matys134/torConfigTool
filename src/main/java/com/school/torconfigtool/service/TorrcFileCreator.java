package com.school.torconfigtool.service;

import com.school.torconfigtool.models.BaseRelayConfig;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class TorrcFileCreator {

    private static final Logger logger = LoggerFactory.getLogger(TorrcFileCreator.class);

    public static void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + config.getNickname());
            writer.newLine();
            String orPort = config.getOrPort() != null ? config.getOrPort() : "127.0.0.1:auto";
            writer.write("ORPort " + orPort);
            writer.newLine();
            writer.write("ContactInfo " + config.getContact());
            writer.newLine();
            writer.write("ControlPort " + config.getControlPort());
            writer.newLine();
            writer.write("SocksPort 0");
            writer.newLine();
            writer.write("RunAsDaemon 1");
            writer.newLine();

            // Write DataDirectory configuration
            String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname();
            writer.write("DataDirectory " + dataDirectoryPath);
            writer.newLine();

            if (config.getBandwidthRate() != null) {
                writer.write("BandwidthRate " + config.getBandwidthRate() + " KBytes");
                writer.newLine();
            }

            if (config instanceof BridgeRelayConfig) {
                config.writeSpecificConfig(writer);
            } else if (config instanceof GuardRelayConfig) {
                config.writeSpecificConfig(writer);
            } else {
                logger.error("Unknown relay type");
            }
        } catch (IOException e) {
            logger.error("Error creating Torrc file", e);
        }
    }
}
