package com.school.torconfigtool.config;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The BridgeConfigWriter interface provides a contract for writing bridge configurations.
 * Classes implementing this interface should provide their own implementation of the writeConfig method.
 *
 * This interface is implemented by different classes like Obfs4ConfigWriter, WebtunnelConfigWriter,
 * and SnowflakeConfigWriter which provide their own implementation of the writeConfig method.
 */
public interface BridgeConfigWriter {

    /**
     * Writes the configuration using the provided BufferedWriter.
     *
     * @param writer the BufferedWriter to use for writing the configuration
     * @throws IOException if an I/O error occurs
     */
    void writeConfig(BufferedWriter writer) throws IOException;
}