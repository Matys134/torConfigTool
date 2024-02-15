package com.school.torconfigtool.bridge.config;

import com.school.torconfigtool.bridge.BridgeConfigWriter;
import com.school.torconfigtool.bridge.config.BridgeRelayConfig;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This class is responsible for writing the configuration for Obfs4 bridges.
 * It implements the BridgeConfigWriter interface.
 */
public class Obfs4ConfigWriter implements BridgeConfigWriter {
    private final BridgeRelayConfig config;

    /**
     * Constructor for the Obfs4ConfigWriter class.
     * @param config The configuration for the bridge relay.
     */
    public Obfs4ConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    /**
     * This method is responsible for writing the configuration.
     * It calls the writeObfs4Config method to perform the actual writing.
     * @param writer The BufferedWriter to write the configuration to.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        writeObfs4Config(writer);
    }

    /**
     * This method writes the Obfs4 configuration to the provided BufferedWriter.
     * @param writer The BufferedWriter to write the configuration to.
     * @throws IOException If an I/O error occurs.
     */
    private void writeObfs4Config(BufferedWriter writer) throws IOException {
        config.writeConfig(writer, "obfs4", "/usr/bin/obfs4proxy", "0.0.0.0:" + config.getServerTransport(), "ContactInfo " + config.getContact());
    }
}